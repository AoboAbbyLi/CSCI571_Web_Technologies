var express = require('express');
const { URL, URLSearchParams } = require('url');
var router = express.Router();
const yelp = require('yelp-fusion');
const client = yelp.client('Xovr8dV2tIhJfshDikkF9oCBSFg6fbHbj4ZxQQbgZ5OQlB2hMUDIpN3nBiUSfRFDhIrQPw0zE5EkwMqskYL7vooSexW8cD5S25QGr1MdVVvQ7jt0M2GKYT6ntNC5WnYx');
const key = "AIzaSyA85lUJmoA1Etkz47obzt2YgZVtegsY0ak";

/* GET home page. */
router.get('/', function(req, res, next) {
	res.render('index', { title: 'Aobo Li CSCI571 HW8' });
	// console.log('get index');
});

router.get('/submit', function(req, res) {
	console.log(req.query);
	var keyword = req.query.keyword;
	var category = req.query.category;
	var distance = req.query.distance * 1609;

	nearbyURL = new URL('https://maps.googleapis.com/maps/api/place/nearbysearch/json');
	nearbyURL.searchParams.append('key', key);
	nearbyURL.searchParams.append('keyword', keyword);
	nearbyURL.searchParams.append('radius', distance);

	if (req.query.category != 'default') {
		nearbyURL.searchParams.append('types', req.query.category);
	}

	if (req.query.from == 'here') {
		nearbyURL.searchParams.append('location', req.query.lat+','+req.query.lng);
		// console.log("nearbyURL"+nearbyURL.href);
		var http=require('https');
		datas = '';
		http.get(nearbyURL.href,function(req2, res2){
		    req2.on('data',function(data){
		    	datas += data;
			});
			req2.on('end',function(){
				res.setHeader('Access-Control-Allow-Origin', '*');
				res.send(datas);
			});
		});
	}
	else {
		geoURL = new URL('https://maps.googleapis.com/maps/api/geocode/json');
		geoURL.searchParams.append('key', key);
		geoURL.searchParams.append('address', req.query.location);
		var http=require('https');
		datas = '';
		// console.log("geoURL"+geoURL.href);
		http.get(geoURL.href,function(req1, res1){
		    req1.on('data',function(data){
		    	datas += data;
			});
			req1.on('end',function(){
				console.log("geodatas"+datas);
				json = JSON.parse(datas);
				geolat = json['results'][0]['geometry']['location']['lat'];
				geolng = json['results'][0]['geometry']['location']['lng'];
				// console.log("geo"+geolat+','+geolng);
				nearbyURL.searchParams.append('location', geolat+','+geolng);
				// console.log("nearbyURL"+nearbyURL.href);
				var http=require('https');
				datas = '';
				http.get(nearbyURL.href,function(req2, res2){
				    req2.on('data',function(data){
				    	datas += data;
					});
					req2.on('end',function(){
						res.setHeader('Access-Control-Allow-Origin', '*');
						res.send(datas);
					});
				});
			});
		});
	};

	// console.log("nearbyURL"+nearbyURL.href);
});

// router.get('/IPlocation', function(req, res) {
// 	var http=require('http');
// 	datas = '';
// 	http.get('http://ip-api.com/json',function(req1, res1){
// 	    req1.on('data',function(data){
// 	    	datas += data;
// 	    	// console.log("data"+data);
// 		});
// 		req1.on('end',function(){
// 			// json = JSON.parse(datas);
// 			// console.log("end");
// 			// console.log("datas1"+datas);
// 			res.setHeader('Access-Control-Allow-Origin', '*');
// 			res.send(datas);			
// 		});
// 	});
// });

router.get('/next_page', function(req, res) {
	// console.log(req.query);
	var next_page_token = req.query.next_page_token;

	nextURL = new URL('https://maps.googleapis.com/maps/api/place/nearbysearch/json');
	nextURL.searchParams.append('key', key);
	nextURL.searchParams.append('pagetoken', next_page_token);

	var http=require('https');
	datas = '';
	http.get(nextURL.href,function(req2, res2){
	    req2.on('data',function(data){
	    	datas += data;
		});
		req2.on('end',function(){
			res.setHeader('Access-Control-Allow-Origin', '*');
			res.send(datas);
		});
	});
});

router.get('/placeIP123', function(req, res) {
	// console.log(req.query);
	var placeID = req.query.placeID;

	placeURL = new URL('https://maps.googleapis.com/maps/api/place/nearbysearch/json');
	placeURL.searchParams.append('key', key);
	placeURL.searchParams.append('placeId', placeID);

	var http=require('https');
	datas = '';
	http.get(placeURL.href,function(req2, res2){
	    req2.on('data',function(data){
	    	datas += data;
		});
		req2.on('end',function(){
			res.setHeader('Access-Control-Allow-Origin', '*');
			res.send(datas);
		});
	});
});

router.get('/yelpMatch', function(req, res) {
	// console.log(req.query);

	client.businessMatch('lookup', {
	  name: req.query.name,
	  city: req.query.city,
	  state: req.query.state,
	  country: req.query.country
	}).then(response => {
		// console.log(response.jsonBody.businesses[0].id);
		res.setHeader('Access-Control-Allow-Origin', '*');
		res.send(response.jsonBody.businesses[0]);
	}).catch(e => {
  		console.log(e);
	});
});

router.get('/yelpReviews', function(req, res) {
	// console.log(req.query);

	// client.reviews(req.query.id).then(response => {
	// 	res.setHeader('Access-Control-Allow-Origin', '*');
	// 	res.send(response.jsonBody.reviews[0]);
	// }).catch(e => {
 //  		console.log(e);
	// });
	client.reviews(req.query.id).then(response => {
	  // console.log(response.jsonBody.reviews[0].text);
	  res.setHeader('Access-Control-Allow-Origin', '*');
		res.send(response.jsonBody.reviews);
	}).catch(e => {
	  console.log(e);
	});
});

module.exports = router;
