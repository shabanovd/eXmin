xquery version "3.0";

import module namespace server="eXmin/server" at "server.xqm";
import module namespace mailing="eXmin/mailing" at "mailing.xqm";

declare namespace eXmin="eXmin";
declare namespace jmx="http://exist-db.org/jmx";
 
declare option exist:serialize "method=xhtml media-type=text/html";

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

declare function local:clean-last-details($server) {
	let $colName := server:last-heartbeat-collection($server)
	let $col := collection($colName)
	let $TS := util:system-dateTime() - xs:dayTimeDuration('PT2H')

	let $res := $col//eXmin:heartbeat[@eXmin:date/xs:dateTime(.) lt $TS]

	return
		for $uri in $res/base-uri(.) return
			xmldb:remove($colName, substring-after($uri, $colName))
};


declare function local:collect-details($server) {
    let $answer := local:send-request($server)

    let $TS := xs:string(util:system-dateTime())

    let $response := $answer[1]
	let $last_hb := server:last-heartbeat-check($server)
	let $hb := 
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

	let $file-name := concat(xsl:format-dateTime($TS, "YYYY-MM-DD'T'hh-mm-ss"),".xml")

	let $tmp := xmldb:store(server:heartbeat-collection($server, $TS), $file-name, $hb)
	let $tmp := xmldb:store(server:last-heartbeat-collection($server), $file-name, $hb)
	let $tmp := local:clean-last-details($server)
	return "done"
};

for $server in //eXmin:server return
	util:eval-async(local:collect-details($server))
