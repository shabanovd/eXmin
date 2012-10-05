xquery version "3.0";

module namespace server='eXmin/server';

import module namespace utils="eXmin/utils" at "utils.xqm";

declare namespace eXmin="eXmin";
declare namespace jmx="http://exist-db.org/jmx";

declare function server:add() {
    (
        <button type="button" class="btn pull-right" data-toggle="modal" data-target="#addServer"><i class="icon-plus"></i> Add server</button>
        ,
        <div class="modal hide" id="addServer" tabindex="-1" role="dialog" aria-labelledby="addServerLabel" aria-hidden="true">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 id="addServerLabel">Add server</h3>
            </div>
            <div class="modal-body">
                <label for="name">Name</label>
                <input type="text" name="name" id="name" class="input-xxlarge"/>
                <label for="URL">URL</label>
                <input type="text" name="url" id="url" class="input-xxlarge"/>
                <label for="JMX">JMX path</label>
                <input type="text" name="JMX" id="JMX" class="input-xxlarge"/>
            </div>
            <div class="modal-footer">
                <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
                <button id="addServerButton" class="btn btn-primary">Add</button>
            </div>
        </div>
        ,
        <script>
            $("#addServerButton").click(function() {{
                $('#addServer').modal('hide');
        		$.ajax({{
	    			type: "POST",
		    		url: "function-call.xq",
                    data: {{call: "server:store-new", name: $("input#name").val(), url: $("input#url").val(), JMX: $("input#JMX").val()}},
                    dataType: "json",
    				success: function (msg) {{
                        alert(msg.message);
				    }},
                    error: function(msg) {{ 
                        alert(msg.message); 
                    }}
			    }});
            }});
        </script>
    )
};

declare function server:last-heartbeat-check($server) {
    let $col := collection(concat("/db/data/",$server/@id))

	let $lastTS := max($col//eXmin:heartbeat/@eXmin:date/xs:dateTime(.))

	return
		$col//eXmin:heartbeat[xs:dateTime(@eXmin:date) eq $lastTS]
};

declare function server:generate-status-bars($details) {

	let $db := $details/jmx:Database
	let $uuid := util:uuid()
	
	return 
	<td>
		<div class="progress" style="margin-bottom: 2px;" onclick="$('#{$uuid}').collapse('toggle')">
			<div class="bar" style="width: {utils:percent($db/jmx:ActiveBrokers, $db/jmx:MaxBrokers)}%;"></div>
		</div>
		<div id="{$uuid}" class="collapse out">
		{
			for $category in distinct-values($details/jmx:Cache/jmx:FileName/text()) order by $category return
			<div>
				{$category,
				for $cache in $details/jmx:Cache[jmx:FileName/text() eq $category] return
				<abbr title="{$cache/jmx:FileName/text(),' [',$cache/jmx:Type/text(),'] Hits:',$cache/jmx:Hits/text(),' Fails:',$cache/jmx:Fails/text()}">
					<div class="progress" style="margin-bottom: 2px;">
						<div class="bar" style="width: {utils:percent($cache/jmx:Used, $cache/jmx:Size)}%;"></div>
					</div>
				</abbr>
				}
			</div>
		}
		</div>
	</td>
};

declare function server:eXist-version($details, $lastCheck) {
	if ($details) then
		let $SystemInfo := $details//jmx:SystemInfo[@name eq "org.exist.management:type=SystemInfo"]
		return concat($SystemInfo/jmx:ExistVersion, ", rev ", $SystemInfo/jmx:SvnRevision)
	else
		()
};

declare function server:java-version($details, $lastCheck) {
	if ($details) then
		$details//jmx:RuntimeImpl//jmx:row[jmx:key eq "java.runtime.version"]/jmx:value/text()
	else
		()
};

declare function server:generate-status-flag($details, $lastCheck) {
	if ($details/@http:status eq "200") then
        if ($lastCheck le xs:dayTimeDuration("PT10M")) then
		    <span class="badge badge-success">Ok</span>
        else
    	    <span class="badge badge-warning">Ok?</span>
	else
        if ($details) then
		    <span class="label label-important">NO RESPONCE</span>
        else
    	    <span class="label label-important">NO CHECK</span>

};

declare function server:status-report() {
    <table class="table">
        <thead>
		    <tr>
			    <th>Name</th>
			    <th>Version</th>
			    <th>Java</th>
				<th>Uptime</th>
				<th>Health</th>
				<th>Sanity report</th>
				<th>Status</th>
				<th>Last check</th>
			</tr>
		</thead>
		<tbody>
		{
		    for $server in //eXmin:server return
			    let $details := server:last-heartbeat-check($server) 
                let $lastCheck := util:system-dateTime() - $details/@eXmin:date/xs:dateTime(.)
				return
				    <tr>
					    <td>{$server/eXmin:name}</td>
					    <td>{server:eXist-version($details, $lastCheck)}</td>
					    <td>{server:java-version($details, $lastCheck)}</td>
						<td>{utils:msecsToDHM($details/jmx:jmx/jmx:Database/jmx:Uptime/text()/number())}</td>
						{server:generate-status-bars($details/jmx:jmx)}
						<td></td>
						<td>{server:generate-status-flag($details, $lastCheck)}</td>
						<td>{utils:durationToDHM( $lastCheck )}</td>
					</tr>
		}
		</tbody>
	</table>
};

declare function server:store-new() {

    let $name := request:get-parameter("name", ())
    let $url := request:get-parameter("url", ())
    let $JMX := request:get-parameter("JMX", ())
    
    return
        if (//eXmin:server[eXmin:url = $url]) then
                <result status="error">
                    <message>{concat("server with URL '",$url,"' already registered.")}</message>
                </result>
        else
            let $uuid := util:uuid()
            
            let $contents := 
                <server xmlns="eXmin" id="{$uuid}">
                    <name>{$name}</name>
                    <url>{$url}</url>
                    <JMX>{$JMX}</JMX>
                </server>
        
            return
                util:catch("*",
                    let $stored := xmldb:store("/db/eXmin/data/servers", concat($uuid, ".xml"), $contents)
                    return
                        <result status="ok">
                            <message>Saved.</message>
                        </result>
                    ,
                    let $message :=
                        replace(
                            replace($util:exception-message, "^.*XMLDBException:", ""),
                            "\[at.*\]$", ""
                        )
                    return
                        <result status="error">
                            <message>{$message}</message>
                        </result>
                )
};
