xquery version "3.0";

import module namespace server="eXmin/server" at "server.xqm";
import module namespace mailing="eXmin/mailing" at "mailing.xqm";

declare namespace eXmin="eXmin";
declare namespace jmx="http://exist-db.org/jmx";
 
declare option exist:serialize "method=xhtml media-type=text/html";

declare function local:mkcol-recursive($collection, $components) {
    if (exists($components)) then
        let $newColl := concat($collection, "/", $components[1])
        return (
            xmldb:create-collection($collection, $components[1]),
            local:mkcol-recursive($newColl, subsequence($components, 2))
        )
    else
        ()
};

declare function local:mkcol($collection, $path) {
    local:mkcol-recursive($collection, tokenize($path, "/"))
};

declare function local:send-request($server) {
    try {
        http:send-request(
	    	<http:request href="{concat($server/eXmin:url/text(),$server/eXmin:JMX/text())}" method="get"/>
	    )
    } catch * {
        <http:response status="fail"/>
    }
};

(: report only ones after server gets up :)
declare function local:mail-server-up($server, $hb) {
    if ($hb/@http:status/string(.) eq "fail") then
        let $mail := mailing:server-up($server)
        return ()
	else
		()
};

(: report only ones after server gets down :)
declare function local:mail-server-down($server, $hb) {
    if ($hb/@http:status/string(.) eq "200") then
        let $mail := mailing:server-down($server)
        return ()
	else
		()
};

declare function local:collect-details($server) {
    let $answer := local:send-request($server)

    let $TS := xs:string(util:system-dateTime())
	let $colName := concat("/data/",$server/@id,"/",substring($TS, 1, 4),"/",substring($TS, 6, 2),"/heartbeats")
	let $tmp := local:mkcol("/db", $colName)
    
    let $response := $answer[1]
	let $last_hb := server:last-heartbeat-check($server)
	return
		xmldb:store(concat("/db", $colName), concat(xsl:format-dateTime($TS, "YYYY-MM-DD'T'hh-mm-ss"),".xml"), 
			<eXmin:heartbeat>
				{attribute {"http:status"} {$response/@status}, attribute {"eXmin:date"} {$TS},
				if ($response/@status eq "200") then
                    let $tmp := local:mail-server-up($server,$last_hb)
					return
						$answer[2]/jmx:jmx

				else 
                    local:mail-server-down($server,$last_hb)
				}
			</eXmin:heartbeat>
		)
};

for $server in //eXmin:server return
	util:eval-async(local:collect-details($server))
