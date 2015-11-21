<html>
<head>
	<title>Das Worker-Interface</title>
	<link rel="stylesheet" href="/stylescreative.css">
</head>
<body>
		<div id="afgstellung"><h1 id="task"><#if exdesc!="">${exdesc}</#if></h1><br /><h1>${task}</h1>
		<#if pic != ""><div id="pic"><img src=${pic}></div></#if>
		<div id="form">
			<form id="hallo" method="GET">
			<div id="wrapper">
				<div id="errorview"></div>
				<input type="text" id="answertext" placeholder="Type your answer here" name="answer"></input><br />
				<#if again!=""><button type="submit" value="Again" id="subagain" class="but" name="button"><img src="http://i.imgur.com/gDS2bGo.png" /></button></#if>
				</div>
				<div id="clear"></div>
				<#if next!=""><button type="submit" value="Next" id="subnext" class="but" name="button">Next</button></#if>
				<#if sub!=""><button type="submit" value="Submit" id="subex" class="but" name="button">Submit and exit</button></#if>
			</form>
		</div>
		

		</div>
		<#if iframe!="">
		<iframe src="${iframe}" id="ifr"></iframe></#if>
</body>
</html>
