xquery version "3.0";

import module namespace server="eXmin/server" at "server.xqm";

declare option exist:serialize "method=json media-type=text/javascript";

let $tmp := util:log-system-out("$call")
let $call := request:get-parameter("call", ())
let $tmp := util:log-system-out($call)
let $res := 
    if ($call) then
        let $function := function-lookup(xs:QName($call), 0)
        let $tmp := util:log-system-out($function)
        (: TODO 
            import
            check permissions 
        :)
        return
            if ($function eq ()) then
                <result status="error">
                    <message>function {$call} not found</message>
                </result>
            else
                $function()
    else
        <result status="error">
            <message>no function name</message>
        </result>

let $tmp := util:log-system-out($res)

return $res
