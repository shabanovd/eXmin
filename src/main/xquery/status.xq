xquery version "3.0";

import module namespace server="eXmin/server" at "server.xqm";

declare namespace eXmin="eXmin";
 
declare option exist:serialize "method=xhtml media-type=text/html";


<html lang="en">
    <head>
        <meta charset="utf-8"/>
        <title>eXmin</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        
        <link rel="stylesheet" href="resources/bootstrap/css/bootstrap.min.css"/>
        <link href="resources/bootstrap/css/bootstrap-responsive.min.css" rel="stylesheet"/>
        
		<style type="text/css">
			body {{
				padding-top: 60px;
				padding-bottom: 40px;
			}}
		</style>
        
        <script src="resources/jquery-1.8.1.min.js"></script>
        <script src="resources/bootstrap/js/bootstrap.min.js"></script>
        
        <script type="text/javascript" src="jquery.pnotify.min.js"></script>
        <link href="jquery.pnotify.default.css" media="all" rel="stylesheet" type="text/css" />
        
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
				<div id="main-area" class="span10">
                    {server:add()}
                    {server:status-report()}
				</div>
    		</div>
    	</div>
        <script type="text/javascript">
            $(function(){{
                $.pnotify({{
                    title: 'Regular Notice',
                    text: 'Check me out! Im a notice.'
                }});
            }});
        </script>
    </body>
</html>