<html>
	<head>
		<title>Worker-Ratings</title>
		<link rel="stylesheet" href="/stylesrating.css">
	</head>
	<body>
	<div id="afgstellung">
		<h1 id="task"><#if exdesc!="">${exdesc}</#if></h1>
		<h1><#if desc!="">${desc}</#if></h1>
		<h2>The question was: </h2><br /><h1>${task}</h1>
		<#if pic != ""><div id="pic"><img src=${pic}></div></#if>
		<form name='ratings' action="/assignment/${expId}" method="POST">
		${ratingTable}
		<div id="buttons">
				<#if again!=""><button type="submit" value="Again" id="subagain" class="but" name="button">Again</button></#if>
				<#if next!=""><button type="submit" value="Next" id="subnext" class="but" name="button">Next</button></#if>
				<#if sub!=""><button type="submit" value="Submit" id="subsub" class="but" name="button">Submit</button></#if>
		</div>
				</form>
		</div>
		<#if iframe!="">
		<iframe src="${iframe}" id="ifr"></iframe></#if>
	</body>
</html>
