xquery version "3.0";

declare namespace eXmin="eXmin";
declare namespace jmx="http://exist-db.org/jmx";
 
declare option exist:serialize "method=xhtml media-type=text/html";

declare function local:collect-details($server) {
	let $col := collection(concat("/db/data/",$server/@id))

	let $lastTS := max($col//eXmin:heartbeat/@eXmin:date/xs:dateTime(.))

	return
		$col//eXmin:heartbeat[xs:dateTime(@eXmin:date) eq $lastTS]
};

declare function local:percent($current, $max) {
    (number($current) div number($max)) * 100
};

declare function local:msecsToDHM($msecs) {
	let $secs := $msecs idiv 1000
	let $mins := $secs idiv 60
	let $secs := $secs - ($mins * 60)
	let $hours := $mins idiv 60
	let $mins := $mins - ($hours * 60)
	let $days := $hours idiv 24
	let $hours := $hours - ($days * 24)

	return
    	concat($days," days ",$hours,":",$mins)
};

declare function local:generate-status-bars($details) {

	let $db := $details/jmx:Database
	let $uuid := util:uuid()
	
	return 
	<td>
		<div class="progress" style="margin-bottom: 2px;" onclick="$('#{$uuid}').collapse('toggle')">
			<div class="bar" style="width: {local:percent($db/jmx:ActiveBrokers, $db/jmx:MaxBrokers)}%;"></div>
		</div>
		<div id="{$uuid}" class="collapse out">
		{
			for $category in distinct-values($details/jmx:Cache/jmx:FileName/text()) order by $category return
			<div>
				{$category,
				for $cache in $details/jmx:Cache[jmx:FileName/text() eq $category] return
				<abbr title="{$cache/jmx:FileName/text(),' [',$cache/jmx:Type/text(),'] Hits:',$cache/jmx:Hits/text(),' Fails:',$cache/jmx:Fails/text()}">
					<div class="progress" style="margin-bottom: 2px;">
						<div class="bar" style="width: {local:percent($cache/jmx:Used, $cache/jmx:Size)}%;"></div>
					</div>
				</abbr>
				}
			</div>
		}
		</div>
	</td>
};

declare function local:generate-status-flag($details) {
	if ($details/@http:status eq "200") then
		<span class="badge badge-success">Ok</span>
	else
		<span class="label label-important">NO RESPONCE</span>

};

let $tmp := ""
return
<html lang="en">
    <head>
        <meta charset="utf-8"/>
        <title>eXmin</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        
        <link rel="stylesheet" href="resources/bootstrap/css/bootstrap.min.css"/>
        
		<style type="text/css">
			body {{
				padding-top: 60px;
				padding-bottom: 40px;
			}}
		</style>
        
        <script src="resources/jquery-1.7.2.min.js"></script>
        <script src="resources/jquery-ui-1.8.20/js/jquery-ui.all.min.js"></script>

        <script src="resources/bootstrap/js/bootstrap.min.js"></script>
        
		<!-- for IE6-8 support of HTML5 elements -->
		<!--[if lt IE 9]>
			<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
		<![endif]-->
    
    </head>
    <body>
    	<nav class="navbar navbar-fixed-top">
    		<div class="navbar-inner">
    			<div class="container">
    				<a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
    					<span class="icon-bar"></span>
					</a>
					<a class="brand" href="#">eXmin</a>
					<div class="nav-collapse">
						<ul class="nav">
							<li><a href="#overview">Overview</a></li>
						</ul>
					</div>
				</div>
			</div>
		</nav>
    	<div class="container">
    		<div class="row-fluid">
    			<div class="span2">
    			    <ul class="nav nav-list">
						<li class="nav-header">
							List header
						</li>
						<li class="active">
							<a href="#">
								<i class="icon-home"></i>
								Home
							</a>
						</li>
						<li>
    						<a href="#">
    							<i class="icon-book"></i>
    							Library
    						</a>
    					</li>
				    </ul>
				</div>
				<div class="span10">
					<table class="table">
						<thead>
							<tr>
								<th>Name</th>
								<th>Location</th>
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
							let $details := local:collect-details($server) 
							let $tmp := util:log-system-out($details/@eXmin:date/xs:string(.))
							return
							<tr>
								<td>{$server/eXmin:name}</td>
								<td></td>
								<td>{local:msecsToDHM($details/jmx:jmx/jmx:Database/jmx:Uptime/text()/number())}</td>
								{local:generate-status-bars($details/jmx:jmx)}
								<td></td>
								<td>{local:generate-status-flag($details)}</td>
								<td>{$details/@eXmin:date/xs:string(.)}</td>
							</tr>
						}
						</tbody>
					</table>
				</div>
    		</div>
    	</div>
    </body>
</html>