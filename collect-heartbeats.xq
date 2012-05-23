xquery version "3.0";

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

declare function local:collect-details($server) {
	let $answer := http:send-request(
		<http:request href="{concat($server/eXmin:url/text(),$server/eXmin:JMX/text())}" method="get"/>
	)
	let $response := $answer[1]
	let $jmx := $answer[2]/jmx:jmx
	let $TS := xs:string(current-dateTime())
	let $tmp := util:log-system-out($TS)
	let $colName := concat("/data/",$server/@id,"/",substring($TS, 1, 4),"/",substring($TS, 6, 2),"/heartbeats")
	let $tmp := local:mkcol("/db", $colName)
	return
		xmldb:store(concat("/db", $colName), concat(xsl:format-dateTime($TS, "YYYY-MM-DD'T'hh-mm-ss"),".xml"), 
			<eXmin:heartbeat>
				{attribute {"http:status"} {$response/@status}, attribute {"eXmin:date"} {$TS},
				if ($response/@status eq "200") then
					$jmx
				else ()
				}
			</eXmin:heartbeat>
		)
};

for $server in //eXmin:server return
	local:collect-details($server)
