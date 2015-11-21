<html>
<head>
	<title>Das Worker-Interface</title>
	<link rel="stylesheet" href="/stylescreative.css">
	<script type="text/javascript">
	function validate(){
		var x = document.forms["answerform"]["answer"].value;
		if(x == null || x == ""){
			document.getElementById("answertext").className = document.getElementById("answertext").className + " error";
			document.getElementById("errorview").innerHTML = "Please don't leave this empty";
			return false;
		}
	}
	</script>
</head>
<body>
		<div id="afgstellung">
		<h1 id="task"><#if exdesc!="">${exdesc}</#if></h1><br /><h1>${task}</h1>
		<#if pic != ""><div id="pic"><img src=${pic}></div></#if>

		<div id="form">
			<form id="hallo" action="/assignment/${expid}" onSubmit="return validate()" method="POST" name="answerform">
			<div id="wrapper">
			<div id="errorview"></div>
				<textarea type="text" id="answertext" placeholder="Type your answer here" name="answer"></textarea><br />
				<#if again!=""><button type="submit" value="Again" id="subagain" class="but" name="button"><img src=http://i.imgur.com/gDS2bGo.png /></button></#if>
				</div>
				<div id="clear"></div>
				<#if next!=""><button type="submit" value="Next" id="subnext" class="but" name="button">Next</button></#if>
				<#if sub!="">
					<#if sub == "only">
					<button type="submit" value="Submit" id="subsub" class="but" name="button">Submit</button>
					<#else>
					<button type="submit" value="Submit" id="subex" class="but" name="button">Submit and exit</button>
					 </#if>
				</#if>
			</form>
		</div>
		</div>
		
		<#if iframe!="">
		<iframe src="${iframe}" id="ifr"></iframe></#if>

</body>
</html>
