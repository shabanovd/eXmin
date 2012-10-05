xquery version "3.0";

declare namespace eXmin="eXmin";
declare namespace jmx="http://exist-db.org/jmx";
declare namespace json="http://www.json.org";

declare option exist:serialize "method=json media-type=text/javascript";

let $end := datetime:timestamp()
let $start := request:get-parameter("start", $end)
let $end := request:get-parameter("end", $end)
let $serverId := request:get-parameter("serverId", util:uuid())
let $callback := request:get-parameter("callback", ())

let $tmp := util:log-system-out($serverId)

let $stDT := datetime:timestamp-to-datetime($start)
let $enDT := datetime:timestamp-to-datetime($end)

let $range := collection(concat('/db/data/',$serverId))//eXmin:heartbeat[@eXmin:date/xs:dateTime(.) gt $stDT and @eXmin:date/xs:dateTime(.) lt $enDT]

let $res :=
<test>
{
		for $item in $range order by $item/@eXmin:date return
			<json:value>
				<json:value json:literal="true">
					{datetime:timestamp($item/@eXmin:date/xs:dateTime(.))}
				</json:value>
				<json:value json:literal="true">
					{$item//jmx:HeapMemoryUsage/jmx:used/xs:long(.)}
				</json:value>
			</json:value>
}
</test>

return $res
