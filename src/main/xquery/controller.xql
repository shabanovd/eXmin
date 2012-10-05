xquery version "1.0";

import module namespace request="http://exist-db.org/xquery/request";
import module namespace xdb = "http://exist-db.org/xquery/xmldb";

declare variable $exist:path external;
declare variable $exist:resource external;
declare variable $exist:controller external;
declare variable $exist:prefix external;
declare variable $exist:root external;

let $action := request:get-parameter("action",())
let $URL : = concat($exist:prefix, $exist:controller, '/')

let $query := request:get-parameter("q", ())

(:
let $tmp := util:log-system-out($action)
let $tmp := util:log-system-out($exist:path)
let $tmp := util:log-system-out($URL)
let $tmp := util:log-system-out(sm:is-externally-authenticated())
:)
return
if ($action eq 'logout') then
	<dispatch xmlns="http://exist.sourceforge.net/NS/exist">
		{session:invalidate()}
		<redirect url="{$URL}"/>
	</dispatch>

else if ($exist:path eq '/login.xql') then
	<ignore xmlns="http://exist.sourceforge.net/NS/exist">
		<cache-control cache="yes"/>
	</ignore>

else if (not (sm:is-externally-authenticated())) then
	<dispatch xmlns="http://exist.sourceforge.net/NS/exist">
		<redirect url="{concat($URL, "login.xql")}"/>
	</dispatch>

else if ($exist:path eq '') then
    <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
        <redirect url="{concat(request:get-uri(), '/')}"/>
    </dispatch>
    
else if ($exist:path eq "/") then
    <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
        <redirect url="status.xq"/>
    </dispatch>


(: Relative path requests from sub-collections are redirected there :)
else if (contains($exist:path, "/resources/")) then
    <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
        <forward url="{$exist:controller}/resources/{substring-after($exist:path, '/resources/')}"/>
    </dispatch>

else
    <ignore xmlns="http://exist.sourceforge.net/NS/exist">
        <cache-control cache="yes"/>
    </ignore>