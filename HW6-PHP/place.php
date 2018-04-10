<!DOCTYPE html>
<html>
<head>
	<title>Travel and Entertainment Search</title>
	<meta http-equiv="content-type" content="text/html; charset=utf-8">
	<style type="text/css">
		h1 {
			margin: 0;
			text-align: center;
			font-style: italic;
			font-weight: normal;
		}
		form {
			border: 2px solid #c8c8c8;
			width: 600px;
			margin-left: 25%;
			margin-bottom: 20px;
			background-color: #F7F7F7;
		}
		td {
			padding-left: 10px;
		}
		.nohref {
			color: black;
			text-decoration: none;
		}
		#searchResult {
			width: 50%;
			margin-left: 25%;
			text-align: center;
		}
		.clickTo {
			/*text-align: center;*/
			margin-top: 20px;
		}
		.arrow {
			/*text-align: center;*/
			/*width: 20px;*/
			height: 20px;
		}
		.hidden {
			display: none;
		}
		.comment {
			text-align: left;
		}
		#formContent {
			padding-top: 8px;
			margin-left: 1%;
			width: 98%;
			border-top: 2px solid #c8c8c8;
			font-weight: bold;
		}
		#other {
			margin-left: 313px;
		}
		#button {
			margin: 20px 60px;
		}
		#searchTable {
			width: 90%;
			margin-left: 5%;
			border-collapse: collapse;
			border: 2px solid #c8c8c8;
		}
		#searchTable, th, td {
			border: 2px solid #c8c8c8;
		}
		#noRecords{
			width: 90%;
			margin-left: 5%;
			background-color: #EFEFEF;
			text-align: center;
			border: 1px solid #DBDBDB;
		}
		.placeTable {
			border-collapse: collapse;
			border: 1px solid #c8c8c8;
			width: 100%;
		}
		.placePhoto {
			width: 95%;
			margin-top: 2.5%;
			margin-bottom: 2.5%;
		}
		#map {
	        height: 250px;
	        width: 300px;
	        position: absolute;
	        left: 0;
	        top: 0;
		}
		.mapBtn {
			height: 25.3px;
			width: 80px;
			padding-left: 9px;
			padding-top: 8px;
			margin: 0;
			cursor:pointer;
		}
		.mapBtn:hover {
			background-color: #DCDCDC;
		}
		#mapButton {
			height: 100px;
			width: 90px;
			padding: 0;
			background-color: #F0F0F0;
		}

	</style>
</head>
<body>
<!-- 	use either GET or POST to transfer the form data to the web server script. -->
<!-- 	The PHP script will retrieve the form inputs, reformat it to the syntax of the API and send it to the Google Places API nearby search service. -->
	<form method="post"><h1>Travel and Entertainment Search</h1>
	<div id="formContent">
		Keyword <input type="text" id="keyword" name="keyword" required><br>
		Category
		<select id="category" name="category">
			<option value="defalut" selected>defalut</option>
			<option value="cafe">cafe</option>
			<option value="bakery">bakery</option>
			<option value="restaurant">restaurant</option>
			<option value="beauty salon">beauty salon</option>
			<option value="casino">casino</option>
			<option value="movie theater">movie theater</option>
			<option value="lodging">lodging</option>
			<option value="airport">airport</option>
			<option value="train station">train station</option>
			<option value="subway station">subway station</option>
			<option value="bus station">bus station</option>
		</select><br>
		Distance (miles) <input type="text" id="distance" name="distance" placeholder="10" value="10" required> from
		<input type="radio" id="here" name="from" value="here" checked onclick="clickHere()">Here<br>
		<input type="radio" id="other" name="from" value="other" onclick="clickOther()">
		<input type="text" id="location" name="location" placeholder="location" required disabled><br>
		<div id="button">
			<input type="submit" id="search" value="Search" disabled="true">
			<button type="button" onclick="resetForm()">Clear</button>
		</div>
	</div>
	</form>

	<div id="result">
	</div>

	<div id="map" class="hidden">
	</div>

	<div id="mapButton" class="hidden">
	</div>

	<div id="temp" hidden>
	</div>

	<?php
		// print_r($_POST);
		// if(isset($_POST['submit'])) {
		if ($_POST) {
			$keyword = $_POST['keyword'];
			$from = $_POST['from'];
			$category = $_POST['category'];
			$distance = $_POST['distance'] * 1609;
			// echo 'submit from data: ' . $keyword . $from . $category . $distance."\n";
			
			// $ip = $_REQUEST['REMOTE_ADDR']; // the IP address to query
			// $query = @unserialize(file_get_contents('http://ip-api.com/php/'.$ip));
			// if($query && $query['status'] == 'success') {
  	// 			echo 'Hello visitor from '.$query['country'].', '.$query['city'].'!';
			// }
			// else {
  	// 			echo 'Unable to get location';
			// }


			if ($from == "here") {
				$lat = $_POST['lat'];
				$lon = $_POST['lon'];
				// echo "here".$lat.$lon;
			}
			else {
				// echo "other";
				$location = $_POST['location'];
				$urlLocation = preg_replace("/\s+/", "+", $location);
				$mapUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=".$urlLocation."&key=AIzaSyB7BCzv4ai3boGU98a9Ccl_HDnWeg-pR5Q";
				// echo "mapUrl".$mapUrl;
				$json = file_get_contents($mapUrl);
				// echo "json".$json;
				$jsonArr = json_decode($json, true);
				// echo "jsonArr".$jsonArr;
				$loc = $jsonArr["results"][0]["geometry"]["location"];
				$lat = $loc["lat"];
				$lon = $loc["lng"];
				// echo $lat.$lon;
			}
			$urlKeyword = preg_replace("/\s+/", "+", $keyword);
			$nearbyUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=AIzaSyBRHFx8Ae7yTntgJ5BsVjZlb7nehM9dleU&location=".$lat.",".$lon."&radius=".$distance."&keyword=".$urlKeyword;
			if ($category != "default") {
				$nearbyUrl .= "&types=".$category;
			}
			$searchJson = file_get_contents($nearbyUrl);
			//You should NOT use PHP code to print any html tags such as table, div, etc. 
			echo "<div id='searchJson'>".$searchJson."</div>";
			
			// $jsonArr = json_decode($json, true);
			//note: empty resutls

			// $jsonPhotos = $jsonArr["results"][0]["photos"];
			// $jsonReviews = $jsonArr["results"][0]["reviews"];
			// for ($photoCount = 0; $photoCount < max(count($jsonPhotos), 5); $photoCount++) {
			// 	$referenceUrl = "https://maps.googleapis.com/maps/api/place/photo?key=AIzaSyBRHFx8Ae7yTntgJ5BsVjZlb7nehM9dleU&photoreference=".$photos[$photoCount]["photo_reference"]."&maxwidth=400";
			// 	$aPhoto = file_get_contents($referenceUrl);
			// 	file_put_contents("photo".$photoCount.".jpg", $aPhoto);
			// 	//png
			// }
			// for ($reviewCount = 0; $reviewCount < max(count($jsonReviews), 5); $reviewCount++) {
				
			// }
			// $results = array();

		}

		if (isset($_GET["placeid"])) {
			$placeid = $_GET["placeid"];
			$placeUrl = "https://maps.googleapis.com/maps/api/place/details/json?placeid=".$placeid."&key=AIzaSyB6TZ3P_Eh1E-Wrj3o-s3MclORju3_Pwac";
			$placeJson = file_get_contents($placeUrl);
			$jsonArr = json_decode($placeJson, true);
			$photoArr = array();
			$reviewArr = array();
			$photos = $jsonArr["result"]["photos"];
			$len = min(count($photos), 5);
			for ($i = 0; $i < $len; $i++) {
				//maxheight = 1600?
				$photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxheight=1600&photoreference=".$photos[$i]["photo_reference"]."&key=AIzaSyB6TZ3P_Eh1E-Wrj3o-s3MclORju3_Pwac";
				$png = file_get_contents($photoUrl);
				file_put_contents("./"$i.".png", $png);
				array_push($photoArr, $i.".png");
			}
			$reviews = $jsonArr["result"]["reviews"];
			$len = min(count($reviews), 5);
			for ($i = 0; $i < $len; $i++) {
				array_push($reviewArr,["name"=>$reviews[$i]["author_name"],"profile"=>$reviews[$i]["profile_photo_url"], "text"=>$reviews[$i]["text"]]);
			}
			$name = $jsonArr["result"]["name"];
			$location = $jsonArr["result"]["geometry"]["location"];
			$details = array("name"=>$name, "location"=>$location, "photos"=>$photoArr, "reviews"=>$reviewArr);
			$reviewJson = json_encode($details);
			echo "<div id='searchName'>".$reviewJson."</div>";
		}

	?>

	<script type="text/javascript">
		// document.getElementById("location").disabled = true;
		// document.getElementById("keyword").required = true;
		// document.getElementById("search").disabled = true;
		function clickHere() {
			document.getElementById("location").disabled = true;
		}
		function clickOther() {
			document.getElementById("location").disabled = false;
		}
		//clear must be done in js
		function resetForm() {
			// document.getElementById("keyword").required = false;
			document.getElementById("keyword").value = "";
			document.getElementById("keyword").required = true;
			document.getElementById("category").options[0].selected = true;
			document.getElementById("distance").value = "10";
			document.getElementById("here").selected = true;
			document.getElementById("location").disabled = true;
			document.getElementById("location").value = "";
			document.getElementById("result").innerHTML = "";
			document.getElementById("temp").innerHTML = "";
			document.getElementById("map").classList.add("hidden");
			document.getElementById("mapButton").classList.add("hidden");
		}
		window.onload = init;
		function init(){

			getlocation();

			submitForm();
            
        }
        function getlocation() {
        	var xhr1 = new XMLHttpRequest();
            xhr1.onreadystatechange = function(){
            	if(xhr1.readyState==4){
            		if (xhr1.status == 200) { 
                		// alert(xhr.responseText);
                		var locObj = JSON.parse(xhr1.responseText);
        				lat = locObj.lat;
        				lon = locObj.lon;
                		// alert(lat + " " + lon);
                		if (lat && lon) {
                			document.getElementById("search").disabled = false;
                		}
                	}
                }
            };
            xhr1.open('get','http://ip-api.com/json');
            xhr1.send(null);
        	
        }
        function submitForm() {
        	var fm = document.getElementsByTagName('form')[0];
            fm.onsubmit = function(){
                //收集表单信息  
                //ajax负责把收集好的信息传递给服务器  
                var fd = new FormData(fm);
                if (typeof lat!="undefined") {
                	fd.append("lat", lat);
                	fd.append("lon", lon);
                }
  
                xhr2 = new XMLHttpRequest();
                xhr2.onreadystatechange = showContents;
                
                xhr2.open('post','./place.php'); 
                //使用FormData无需设置header头  
                //xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");  
                xhr2.send(fd);
                //阻止浏览器默认动作 跳转 
                return false;
            }
        }
        function showContents(){
   //      	var tempDiv = document.createElement("div");
			// var pageDiv = document.getElementById("pictureBar");
            if(xhr2.readyState==4){
            	if (xhr2.status == 200) { 
                	// alert(xhr2.responseText);
                	// document.write(xhr2.responseText);
                	// document.getElementById("result").innerHTML = xhr2.responseText;
                	// document.getElementById("result").innerHTML = "something new";
                	document.getElementById("temp").innerHTML = xhr2.responseText;
                	// var outMsg = (xhr.responseXML && xhr.responseXML.contentType=="text/xml") ? xhr.responseXML.getElementsByTagName("choices")[0].textContent : xhr.responseText;

					var searchJson = document.getElementById("searchJson").innerHTML;
					//var searchJson = <php if(isset($searchJson)) { echo $searchJson; } else {echo "";} ?>
                	// document.getElementById("result").innerHTML = "something new";

					// alert("js "+ searchJson);
					var jsonObj = JSON.parse(searchJson);
					if (jsonObj["results"].length == 0) {
						document.getElementById("result").innerHTML = "<div id='noRecords'>No records have been found</div>";
					}
					else {
						var tableContent = "<table id='searchTable' border='1'><tr><th>Category</th><th>Name</th><th>Address</th></tr>";
					
						for (var i = 0; i < jsonObj["results"].length; i++) {
							var icon = jsonObj["results"][i]["icon"];
	        				// var name = "<button class='name' onclick='clickName(" + jsonObj["placeid"] + ")'>" + jsonObj["results"][i]["name"] + "</button>";
	        				// var name = "<span class='name' onclick='clickName(" + jsonObj["results"][i]["place_id"] + ");'>" + jsonObj["results"][i]["name"] + "</span>";
	        				var name = "<a class='nohref' href='javascript:void(0)' onclick=clickName('" + jsonObj["results"][i]["place_id"] + "');> " + jsonObj["results"][i]["name"] + "</a>";
	        				// var addr = jsonObj["results"][i]["vicinity"];
	        				var loc = jsonObj["results"][i]["geometry"]["location"];
	        				var addr = "<a id= '"+i+"' class='nohref' href='javascript:void(0)' onclick=clickAddress('"+loc['lat']+"','"+loc['lng']+ "','"+i+"');> " + jsonObj["results"][i]["vicinity"] + "</a>";
	        				tableContent += "<tr><td><img src='" + icon + "' height='40px' width='50px'></td><td> " + name + "</td><td> " + addr + "</td></tr>";
						}
						tableContent += "</table>";
						document.getElementById("result").innerHTML = tableContent;
					}
					
        				
				}
				else {
					alert("There was a problem with the request " + xhr2.status);
				}
			}
        }

        function clickName(placeid) {
        	var xhr3 = new XMLHttpRequest();
            xhr3.onreadystatechange = function(){
            	if(xhr3.readyState==4){
            		if (xhr3.status == 200) { 
                		//显示name详情
                		//alert("'"+<php echo $_GET["placeid"];?>+"'");
                		document.getElementById("temp").innerHTML = xhr3.responseText;
                		showPlaceDetail();
                	}
                }
            };
            // var data = "placeid="+placeid;
            // xhr3.open('get',"'"+<php echo $SERVER["PHP_SELF"]; ?>+"'");
            xhr3.open('get',"./place.php?placeid="+placeid);
            xhr3.send();
            return false;
        }

        function showPlaceDetail() {
        	json = document.getElementById("searchName").innerHTML;
        	placeJsonObj = JSON.parse(json);
        	//整个文字+箭头是button 改
        	var content = "<div id='searchResult'><b>"+placeJsonObj["name"]+"</b><br><br>";
        	content += "<a id='reviewDown' class='nohref' href='javascript:void(0)' onclick=clickReviewDown();><div class='clickTo'>click to show reviews</div><img class='arrow' src='http://cs-server.usc.edu:45678/hw/hw6/images/arrow_down.png'></a><a id='reviewUp' class='hidden nohref' href='javascript:void(0)' onclick=clickReviewUp();><div class='clickTo'>click to hide reviews</div><img id='reviewArrowUp' class='arrow' src='http://cs-server.usc.edu:45678/hw/hw6/images/arrow_up.png' onclick=clickReviewUp();></a><div id='reviewContent'></div>";
        	content += "<a id='photoDown' class='nohref' href='javascript:void(0)' onclick=clickPhotoDown();><div class='clickTo'>click to show photos</div><img class='arrow' src='http://cs-server.usc.edu:45678/hw/hw6/images/arrow_down.png'></a><a id='photoUp' class='hidden nohref' href='javascript:void(0)' onclick=clickPhotoUp();><div class='clickTo'>click to hide photos</div><img class='arrow' src='http://cs-server.usc.edu:45678/hw/hw6/images/arrow_up.png'></a><div id ='photoContent'></div>";
        	content += "</div>";
        	document.getElementById("result").innerHTML = content;
        }

        function clickReviewUp() {
        	document.getElementById("reviewUp").classList.add("hidden");
        	document.getElementById("reviewDown").classList.remove("hidden");
        	document.getElementById("reviewContent").classList.add("hidden");
        }
        function clickReviewDown() {
        	document.getElementById("reviewUp").classList.remove("hidden");
        	document.getElementById("reviewDown").classList.add("hidden");
        	document.getElementById("reviewContent").classList.remove("hidden");
        	document.getElementById("photoUp").classList.add("hidden");
        	document.getElementById("photoDown").classList.remove("hidden");
        	document.getElementById("photoContent").classList.add("hidden");

        	var content = "<table class='placeTable'>";
        	if (placeJsonObj["reviews"].length == 0) {
        		content += "<tr><th>No Reviews Found</th></tr>";
        	}
        	else {
        		for (var i = 0; i < placeJsonObj["reviews"].length; i++) {
	        		content += "<tr><td>" + "<img src='"+ placeJsonObj["reviews"][i]["profile"]+ "'height='30px'> " + placeJsonObj["reviews"][i]["name"] + "</td></tr>";
	        		content += "<tr><td class='comment'>" + placeJsonObj["reviews"][i]["text"] + "</td></tr>";
        		}
        	}
        	content += "</table>";
        	document.getElementById("reviewContent").innerHTML = content;
        }
        function clickPhotoUp() {
        	document.getElementById("photoUp").classList.add("hidden");
        	document.getElementById("photoDown").classList.remove("hidden");
        	document.getElementById("photoContent").classList.add("hidden");
        }
        function clickPhotoDown() {
        	document.getElementById("photoUp").classList.remove("hidden");
        	document.getElementById("photoDown").classList.add("hidden");
        	document.getElementById("photoContent").classList.remove("hidden");
        	document.getElementById("reviewUp").classList.add("hidden");
        	document.getElementById("reviewDown").classList.remove("hidden");
        	document.getElementById("reviewContent").classList.add("hidden");
        	var content = "<table class='placeTable'>";
        	if (placeJsonObj["photos"].length == 0) {
        		content += "<tr><th>No Photos Found</th></tr>";
        	}
        	else {
        		for (var i = 0; i < placeJsonObj["photos"].length; i++) {
	        		content += "<tr><td>" + "<a target='_blank' href='./" + placeJsonObj["photos"][i] + "'><img class='placePhoto' src='./"+ placeJsonObj["photos"][i]+ "?t="+Math.random()+ "'></a></td></tr>";
	        	}
	        }
        	content += "</table>";
        	document.getElementById("photoContent").innerHTML = content;
        }

        function clickAddress(lat, lng, id) {
        	if (document.getElementById("map").classList.contains("hidden")) {
	    		var map = document.getElementById("map");
	    		var ele = document.getElementById(id).offsetParent;
	    		eleLeft = 10;
	    		eleTop = Math.round(ele.offsetHeight / 3 * 2);
	    		// eleTop = 33.3;
	    		while (ele != null) {
	    			eleLeft += ele.offsetLeft + ele.clientLeft;
	    			eleTop += ele.offsetTop + ele.clientTop;
	    			ele = ele.offsetParent;
	    		}
	    		map.style.left = eleLeft+"px";
	    		map.style.top = eleTop+"px";
	        	initMap(lat, lng);
	        	document.getElementById("map").classList.remove("hidden");
			}
        	else {
      			document.getElementById("map").classList.add("hidden");
      			document.getElementById("mapButton").classList.add("hidden");
      		}
        }

        function initMap(lat, lng) {
			var uluru = {lat: Number(lat), lng: Number(lng)};
	        // var uluru = {lat: -25.363, lng: 131.044};
	        // {lat: placeJsonObj["location"]["lat"], lng: placeJsonObj["location"]["lng"]};
	        var map = new google.maps.Map(document.getElementById('map'), {
	          zoom: 4,
	          center: uluru
	        });
	        var marker = new google.maps.Marker({
	          position: uluru,
	          map: map
	        });

	        initMapBtn(lat, lng);
      	}

      	function initMapBtn(locLat, locLng) {
      		// var content = "<div id='mapButton'><div class='mapBtn' href='javascript:void(0)' onclick=walk('"+lat+"','"+lng+"');><a class='nohref' > Walk there </a></div><div class='mapBtn' href='javascript:void(0)' onclick=bike('"+lat+"','"+lng+"');><a class='nohref' > Bike there </a></div><div class='mapBtn' href='javascript:void(0)' onclick=drive('"+lat+"','"+lng+"');><a class='nohref' > Drive there </a></div></div>"
  			// var content = "<div class='mapBtn' href='javascript:void(0)' onclick=walk('"+locLat+"','"+locLng+"');><a class='nohref' > Walk there </a></div><div class='mapBtn' href='javascript:void(0)' onclick=bike('"+locLat+"','"+locLng+"');><a class='nohref' > Bike there </a></div><div class='mapBtn' href='javascript:void(0)' onclick=drive('"+locLat+"','"+locLng+"');><a class='nohref' > Drive there </a></div>"
  			var content = "<div class='mapBtn' href='javascript:void(0)' onclick=walk();><a class='nohref' > Walk there </a></div><div class='mapBtn' href='javascript:void(0)' onclick=bike();><a class='nohref' > Bike there </a></div><div class='mapBtn' href='javascript:void(0)' onclick=drive();><a class='nohref' > Drive there </a></div>"
      		mapButton = document.getElementById("mapButton");
      		mapButton.innerHTML = content;
      		mapButton.style.position = "absolute";
      		mapButton.style.left = eleLeft+"px";
      		mapButton.style.top = eleTop+"px";
      		mapButton.classList.remove("hidden");

      		start = new google.maps.LatLng(Number(lat), Number(lon));
			end = new google.maps.LatLng(Number(locLat), Number(locLng));
			directionsService = new google.maps.DirectionsService();
      	}

      	var directionsDisplay;
      	var map;

      	function walk() {
			initialize();
			calcRoute("WALKING");
      	}
      	function bike() {
			initialize();
			calcRoute("BICYCLING");
      	}
      	function drive() {
			initialize();
			calcRoute("DRIVING");
      	}

		function initialize() {
		  directionsDisplay = new google.maps.DirectionsRenderer();
		  var mapOptions = {
		    zoom: 4,
		    center: start
		  }
		  map = new google.maps.Map(document.getElementById('map'), mapOptions);
		  directionsDisplay.setMap(map);
		}

		function calcRoute(mode) {
		  var request = {
		      origin: start,
		      destination: end,
		      travelMode: mode
		  };
		  directionsService.route(request, function(response, status) {
		    if (status == 'OK') {
		      directionsDisplay.setDirections(response);
		    }
		  });
		}

      	



	</script>
	<script
		async defer
		src ="https://maps.googleapis.com/maps/api/js?key=AIzaSyB6TZ3P_Eh1E-Wrj3o-s3MclORju3_Pwac">
	</script>

</body>
</html>