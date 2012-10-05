xquery version "3.0";

declare namespace chart-memory='eXmin/chart/memory';

declare option exist:serialize "method=xhtml media-type=text/html";

let $serverId := request:get-parameter("serverId", ())

return
<html lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title>eXmin - memory</title>

		<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
		<script type="text/javascript" src="resources/highstock/highstock.js"></script>
<script type="text/javascript">
	window.serverId = "{$serverId}";
</script>
	</head>
	<body>

		<div id="container" style="height: 500px; min-width: 600px"></div>

		<script type="text/javascript" src="memory.chart.js"></script>
	</body>
</html>