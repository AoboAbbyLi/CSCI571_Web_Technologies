import { Component, Input, OnChanges, SimpleChange, Output, EventEmitter, ElementRef, ViewChild, NgModule, ChangeDetectorRef } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import { FormGroup, FormBuilder, FormControl, Validators, NgForm} from '@angular/forms';
import { trigger, state, style, animate, transition, keyframes} from '@angular/animations';

import * as $ from 'jquery';
import * as moment from 'moment';

declare var google: any;
declare var Bricklayer: any;
declare var Masonry:any;

@Component({
  selector: 'app-details',
  templateUrl: './details.component.html',
  styleUrls: ['./details.component.css'],
  animations: [
	    trigger('reviewsAnimation', [
	      state('google' , style({ opacity: 1})),
	      state('yelp' , style({ opacity: 1 })),
	      transition('google => yelp', [
		      animate(500, keyframes([
	          style({ opacity: 0}),
	          style({ opacity: 1})
        	]))
		    ]),
		    transition('yelp => google', [
		      animate(500, keyframes([
	          style({ opacity: 0}),
	          style({ opacity: 1})
        	]))
		    ]),
	    ])
	    // trigger('reviewsAnimation', [
	    //   state('google' , style({ opacity: 1})),
	    //   state('yelp' , style({ opacity: 1 })),
	    //   transition('google => yelp', [
		   //    animate(500, keyframes([
	    //       style({ opacity: 0}),
	    //       style({ opacity: 1})
     //    	]))
		   //  ]),
		   //  transition('yelp => google', [
		   //    animate(500, keyframes([
	    //       style({ opacity: 0}),
	    //       style({ opacity: 1})
     //    	]))
		   //  ]),
	    // ])
  	]
})
export class DetailsComponent implements OnChanges {

	@Input() place;
	// @Input() favoriteArr;
	@Output() list = new EventEmitter();
	@Output() favDeleteEmitter = new EventEmitter();
	@Output() openHoursEmitter = new EventEmitter();
	// @Output()) infoEmitter = new EventEmitter();
	// @Output() favorite = new EventEmitter();
	// @Output() allPlacesEmitter = new EventEmitter();

	searchFail;
	yelpFail;

	selected = "info";
	// selected;

	// placeID;
	placeData;
	placeName;
	info;
	photos;
	reviews;
	// nav = "info";
	text;	
	// hoursDisplay: boolean = false;

	map;
	uluru;
	street;
	@ViewChild('mapf') mapForm: NgForm;
	@ViewChild('inputFrom') inputFrom: ElementRef;

	// marker;
	// fromValue = "Your location";
	autocomplete;
	service;
	routeTo;
	from;
	mode;
	mapDisplay: boolean = true;
	formInputChange = false;

	sourceSelected = "Google Reviews";
  orderSelected = "Default Order";
  sourceClicked = "google";

  yelpMatchData;
  yelpReviewsData;
  yelpReviews;

  googleDefaultReviews;
  googleLowestRatingReviews;
  googleHighestRatingReviews;
  googleMostRecentReviews;
  googleLeastRecentReviews;
  yelpDefaultReviews;
  yelpLowestRatingReviews;
  yelpHighestRatingReviews;
  yelpMostRecentReviews;
  yelpLeastRecentReviews;

  bricklayer;

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type':  'application/json'
    })
  };

  serverUrl = "http://aobolihw8-env.us-west-1.elasticbeanstalk.com";

	constructor(
		private http: HttpClient,
		private ref: ChangeDetectorRef
	) { }

  ngOnChanges(changes: {[propKey: string]: SimpleChange}) {
  	let placeProp = changes['place'];
  		console.log("details place"+this.place);
		if (placeProp){
			if(placeProp.isFirstChange()) {
			}
			else {
				// console.log("details place change");
				this.updateDetails();
				// this.clickInfo();
			}
		}
  }

  clickList() {
  	this.list.emit(true);
  }

  updateDetails() {
  	// console.log("nav"+this.nav);
  	// var params = new HttpParams()
	  //   .set('placeID', this.placeID);

  	// this.http.get(this.serverUrl+"/placeID", {params}).subscribe(data => {
  	// 	this.placeData = data;
  	// 	this.placeName = data['name'];
  	// });
  	// this.placeID = this.place['placeID'];
  	if (this.place) {
  		console.log("updateDetails"+this.place);
  		this.searchFail = false;
	  	this.placeName = this.place['name'];
	  	this.selected = "info";
	  	this.formInputChange = false;
	  	this.initializeMap();
	  	// this.requestData();
	  	this.sourceSelected = "Google Reviews";
	    this.orderSelected = "Default Order";
	    this.sourceClicked = "google";
	    this.reviews = null;
	    this.yelpReviews = null;
	    this.googleDefaultReviews = null;
		  this.googleLowestRatingReviews = null;
		  this.googleHighestRatingReviews = null;
		  this.googleMostRecentReviews = null;
		  this.googleLeastRecentReviews = null;
		  this.yelpDefaultReviews = null;
		  this.yelpLowestRatingReviews = null;
		  this.yelpHighestRatingReviews = null;
		  this.yelpMostRecentReviews = null;
		  this.yelpLeastRecentReviews = null;
	    this.photos = null;
	    this.yelpFail = false;
	    this.searchFail = false;
	    // this.fromValue = "Your location";
	    // this.nav = "info";
	    // document.getElementById('nav-info').classList.add("active");
	    (<HTMLInputElement>document.getElementById('inputFrom')).value = "Your location";
	    (<HTMLSelectElement>document.getElementById('selectMode')).options[0].selected = true;
	    (<HTMLElement>document.getElementById('directionsPanel')).innerHTML = "";
	    // document.getElementById('nav-info').tab('show');
	    // console.log("fromValue changes to your location");
  	}
  	
  }

  initializeMap() {
	  this.uluru = new google.maps.LatLng(this.place['lat'],this.place['lng']);

	  this.map = new google.maps.Map(document.getElementById('map'), {
      center: this.uluru,
      zoom: 15,
      // streetViewControl: false
    });
	  let marker = new google.maps.Marker({
      position: this.uluru,
      map: this.map
    });

	  // var panorama = this.map.getStreetView();
	  // panorama.setPosition(this.uluru);
	  // panorama.setPov(/** @type {google.maps.StreetViewPov} */({
	  //   heading: 0,
	  //   pitch: 0,
	  // }));
	  // console.log("visible1"+panorama.getVisible());
	  // panorama.setVisible(true);
	  // console.log("visible2"+panorama.getVisible());

	  var streetMap = new google.maps.Map(document.getElementById('street'), {
      center: this.uluru,
      zoom: 15,
      // streetViewControl: false
    });

    this.street = new google.maps.StreetViewPanorama(
      document.getElementById('street'), {
        position: this.uluru,
        pov: {
          heading: 0,
          pitch: 0
        },
        visible: true
      });
  	streetMap.setStreetView(this.street);
  	// street.setVisible(true);
	  this.requestData();
	}

	requestData() {
  	var request = {
		  placeId: this.place['placeID']
		};
		// console.log(this.placeID);
		// console.log("placeName"+this.placeName);
  	this.service = new google.maps.places.PlacesService(this.map);
  	// service = new google.maps.places.PlacesService();
  	this.service.getDetails(request, this.callback.bind(this));

  }

 	callback(placeDetails, status) {
	  if (status == google.maps.places.PlacesServiceStatus.OK) {
	    // createMarker(this.place);
	    this.placeData = placeDetails;
	    this.updateInfo();
	    // console.log("update placeName"+this.placeName);
	    // console.log("Google place" + placeDetails.name);
	    // console.log("Google place" + placeDetails.place_id);
	    // console.log("Google place" + placeDetails.rating);
	    this.updatePhotos();
	    this.updateReviews();
	    this.updateMap();
	    this.ref.markForCheck();
    	this.ref.detectChanges();
    	// this.bricklayer = new Bricklayer(document.querySelector('.bricklayer'));
  //   	var elem = document.querySelector('.grid');
  //   	var msnry = new Masonry('.grid', {
		//   // options
		//   itemSelector: ".grid-item",
		//   columnWidth: 50%，
		//   horizontalOrder: true,
		//   percentPosition: true, 
		// });
	  }
	  else {
	  	this.searchFail = true;
	  }
	}
	updateInfo() {
	    this.text = "Check out "+encodeURIComponent(this.placeData['name'])+" located at "+encodeURIComponent(this.placeData['formatted_address'])+". "+"Website:";
		// let info = Array();
	    // if (this.placeData.formatted_address != undefined) {
	    // 	info.push({key : 'Address', value : this.placeData.formatted_address});
	    // }
	    // if (this.placeData.international_phone_number != undefined) {
	    // 	info.push({key : 'Phone Number', value : this.placeData.international_phone_number});
	    // }
	    if (this.placeData.price_level != undefined) {
	    	// info.push({key : 'Price Level', value : this.placeData.price_level});
	    	let price = "";
	    	for (let i = 1; i <= this.placeData.price_level; i++) {
	    		price += "$";
	    	}
	    	this.placeData.price = price;
	    }
	    if (this.placeData.rating != undefined) {
	    	// info.push({key : 'Rating', value : this.placeData.rating});
	    	// (<HTMLElement>document.querySelector(".stars-inner")).style.width = Number(this.placeData.rating)/5.0*100 + "%";
	    	// (<HTMLElement>document.getElementById("inner")).style.width = Number(this.placeData.rating)/5.0*100 + "%";
	    	// (<HTMLElement>document.getElementsByClassName("stars-inner")[0]).style.width = Number(this.placeData.rating)/5.0*100 + "%";
	    	// let content = "<div class='stars-outer'><div class='stars-inner'></div></div>";
	    	// info.push({key : 'Rating', value : content});
	    	this.placeData.ratingWidth = Number(this.placeData.rating)/5.0*100 + "%";
	    	// document.getElementsByClassName("stars-inner")[0].style.width = this.placeData.ratingAttr;
	    	console.log("ratingWidth"+this.placeData.ratingWidth);
	    }
	    // if (this.placeData.url != undefined) {
	    // 	info.push({key : 'Google Page', value : "<a href="+this.placeData.url+">"+this.placeData.url+"</a>"});
	    // }
	    // if (this.placeData.website != undefined) {
	    // 	info.push({key : 'Website', value : "<a href="+this.placeData.website+">"+this.placeData.website+"</a>"});
	    // }
	    if (this.placeData.opening_hours != undefined) {
	    	let hours = Array();
	    	let opening = this.placeData.opening_hours.weekday_text;
	    	for (let i = 0; i < opening.length; i++) {
	    		let results = opening[i].split(": ");
	    		hours.push(results);
	    	}
	    	let days = Array("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
	    	let now = moment().utcOffset(Number(this.placeData.utc_offset));
	    	// console.log("now"+now.format());
	    	// console.log("offSet"+Number(this.placeData.utc_offset));
	    	// console.log("offSet"+this.placeData.utc_offset);
	    	let day = now.day();
	    	// console.log("day1"+ day);
	    	let weekday = days[day];
	    	for (let i = 0; i < hours.length; i++) {
	    		if (hours[i][0] == weekday) {
	    			if (hours[i][1] == "Closed") {
	    				if (this.placeData.opening_hours.open_now) day--;
	    			}
	    			else {
	    				let openHour = moment(hours[i][1].split(" - ")[0], "h:mm A").hour();
		    			let openMin = moment(hours[i][1].split(" - ")[1], "h:mm A").minute();
		    	// console.log("open"+ open);
		    	// console.log("now.hour"+ now.hour());
		    			// if (openHour > now.hour() || (openHour == now.hour()&&openMin > now.minute())) day--;
		    			if (this.placeData.opening_hours.open_now && (openHour > now.hour() || (openHour == now.hour()&&openMin > now.minute()))) day--;
		    			break;
	    			}
	    		}
	    	}
	    	//moment: 0->Sunday google.place.arr:0->Monday
	    	day--;
	    	if (day == -1) day = 6;
	    	console.log("day2"+day);
	    	let arr1 = hours.slice(0, day);
	    	let arr2 = hours.slice(day);
	    	let rearrangedHours = arr2.concat(arr1);
	    	// let local = opening_hours.utc_offset;
	    	
	    	this.placeData.hours = rearrangedHours;

	    	// info.push({key : 'opening_hours', value : this.placeData.opening_hours});
	    }
	    // this.info = info;
	    // this.selected = "info";
	    // console.log("this.selected"+this.selected);
	    // console.log("details info address"+this.info[0].key+this.info[0].value);
	}
	updatePhotos() {
		let photos = Array();
		if (this.placeData.photos) {
			for (var i = 0; i < this.placeData.photos.length; i++) {
				photos.push(this.placeData.photos[i].getUrl({'maxHeight': 1600}));
			}
		}
		console.log("details photos"+photos);
		this.photos = photos;
	}
	updateMap() {
		this.mapDisplay = true; //街景地图
    // this.fromValue = "Your location";
	  this.routeTo = this.placeData['name']+", "+this.placeData['formatted_address'];
		var input = $("#inputFrom")[0];
		// var input = <HTMLInputElement>document.getElementById('inputFrom');
		// var input = this.inputFrom;
		this.autocomplete = new google.maps.places.Autocomplete(input);
	}
	onFormSubmit(mapForm: NgForm) {
		this.from = (<HTMLInputElement>document.getElementById('inputFrom')).value;
		if (this.from == "Your location" || this.from == "My location") {
			this.http.get("http://ip-api.com/json").subscribe(data => {
	  		let lat = data['lat'];
	  		let lng = data['lon'];
	  		this.from = new google.maps.LatLng(lat, lng);
	  		// this.mode = mapForm.value.selectMode;
				this.mode = (<HTMLInputElement>document.getElementById('selectMode')).value;
				// this.writeObj(mapForm.form);
				// console.log(mapForm.form.value);
				this.updateRoute();
					// this.from = this.uluru;
	  	});
		}
		else {
			// this.mode = mapForm.value.selectMode;
			this.mode = (<HTMLInputElement>document.getElementById('selectMode')).value;
			// this.writeObj(mapForm.form);
			// console.log(mapForm.form.value);
			this.updateRoute();
		}
		
	}
	writeObj(obj){ 
		 var description = ""; 
		 for(var i in obj){ 
		 var property=obj[i]; 
		 description+=i+" = "+property+"\n"; 
		 } 
		 console.log(description);
	} 
	updateRoute() {
		// var autoFrom = this.autocomplete.getPlace();
		// console.log(autoFrom);
		// console.log(this.writeObj(autoFrom));
    // if (autoFrom && autoFrom.place_id) {
    // 	this.from = {'placeId': autoFrom.place_id};
    // 	console.log(this.from);
    // }
    	console.log(this.from);
		let DirectionsRequest = {
			// origin: this.inputFrom.value,
			origin: this.from,
			destination: {'placeId': this.place['placeID']},
			travelMode: this.mode,
			provideRouteAlternatives: true
		}
		// console.log(this.from+" " this.place['placeID']+" "+ this.mode);
		var directionsService = new google.maps.DirectionsService();
		var directionsDisplay = new google.maps.DirectionsRenderer();
		let map = new google.maps.Map(document.getElementById('map'), {
	      center: this.uluru,
	      zoom: 15
	    });
		directionsDisplay.setMap(map);
		(<HTMLElement>document.getElementById('directionsPanel')).innerHTML = "";
		directionsDisplay.setPanel(document.getElementById('directionsPanel'));
		directionsService.route(DirectionsRequest, function(result, status) {
    if (status == 'OK') {
      directionsDisplay.setDirections(result);
    }
  });
		 
	}
	updateReviews() {
		this.yelpReviews = null;
		this.reviews = null;
		console.log(this.yelpReviews);
		console.log(this.reviews);
		this.getGoogleReviews();
		this.getYelpMatch();
	}
	getGoogleReviews() {
		let reviews = Array();
		if (this.placeData.reviews) {
			for (var i = 0; i < this.placeData.reviews.length; i++) {
				let review = this.placeData.reviews[i];
				// let day = moment.unix(review['time']).format('YYYY-MM-DD H:mm:ss');
				let day = moment(review['time'], "X").format("YYYY-MM-DD HH:mm:ss");;
				// console.log("rating"+review['rating']);
				// let rating = "";
				// for (let j = 1; j <= review['rating']; j++) {
				// 	rating += '<i class="fas fa-star"></i>';
				// }
				let reviewArr = {
					author_name: review['author_name'], 
					// ratingInt: Array.apply(null, Array(parseInt(review['rating']))),
					// ratingPoint: review['rating']-parseInt(review['rating']),
					rating: review['rating'],
					ratingArr: Array.apply(null, Array(review['rating'])),
					// rating: rating,
					time: day, 
					unix: review['time'],
					text: review['text'],
					author_url: review['author_url'],
					profile_photo_url: review['profile_photo_url']
				};
				// reviews.push(this.placeData.reviews[i]);
				reviews.push(reviewArr);
			}
		}
		console.log("details reviews"+reviews);
		this.reviews = reviews;
		this.googleDefaultReviews = reviews.concat();
		this.googleHighestRatingReviews = reviews.concat().sort((a, b)=>{return b.rating - a.rating});
		// console.log(this.googleHighestRatingReviews[0].rating +" "+ this.googleHighestRatingReviews[1].rating+" "+this.googleHighestRatingReviews[2].rating+" "+this.googleHighestRatingReviews[3].rating+" "+this.googleHighestRatingReviews[4].rating)
		this.googleLowestRatingReviews = reviews.concat().sort((a, b)=>{return a.rating - b.rating});
		// console.log(this.googleLowestRatingReviews[0].rating +" "+ this.googleLowestRatingReviews[1].rating+" "+this.googleLowestRatingReviews[2].rating+" "+this.googleLowestRatingReviews[3].rating+" "+this.googleLowestRatingReviews[4].rating)
		this.googleMostRecentReviews = reviews.concat().sort((a, b)=>{return b.unix - a.unix});
		this.googleLeastRecentReviews = reviews.concat().sort((a, b)=>{return a.unix - b.unix});
	}
	// clickInfo() {
	// 	this.selected = 'info';
	// }
	// clickPhotos() {
	// 	this.selected = 'photos';
	// }
	// clickMap() {
	// 	this.selected = 'map';
	// }
	// clickReviews() {
	// 	this.selected = 'reviews';
	// }
	getYelpMatch() {
		let arr = this.placeData.formatted_address.split(", ");
    // var params = new HttpParams()
    //   .set('name', this.placeData.name)
    //   .set('city', this.placeData.address_components[3].short_name)
    //   .set('state', this.placeData.address_components[5].short_name)
    //   .set('country', this.placeData.address_components[6].short_name);
    var params = new HttpParams()
      .set('name', this.placeData.name)
      .set('city', arr[arr.length-3])
      .set('state', arr[arr.length-2].split(" ")[0])
      .set('country', 'US');
    this.http.get(this.serverUrl+"/yelpMatch", {params}).subscribe(data => {
      this.yelpMatchData = data;
      if (data != null && data['id'] != undefined) {
        this.getYelpReviews();
      }
    },error => {
    	this.yelpFail = true;
    });
  }
  getYelpReviews() {
    var params = new HttpParams()
      .set('id', this.yelpMatchData.id)
    this.http.get(this.serverUrl+"/yelpReviews", {params}).subscribe(data => {
      this.yelpReviews = data;
      console.log("this.yelpReviews.length"+this.yelpReviews.length);
      for (let i = 0; i < this.yelpReviews.length; i++){
      	let review = this.yelpReviews[i];
    //   	let rating = "";
				// for (let j = 1; j <= review['rating']; j++) {
				// 	rating += '<i class="fas fa-star"></i>';
				// }
				let rating = Array.apply(null, Array(review['rating']));
				this.yelpReviews[i].ratingArr = rating;
				// let unix = moment(review['time_created', "YYYY-MM-DD HH:mm:ss"]);
				this.yelpDefaultReviews = this.yelpReviews.concat();
				this.yelpHighestRatingReviews = this.yelpReviews.concat().sort((a, b)=>{return b.rating - a.rating});
				this.yelpLowestRatingReviews = this.yelpReviews.concat().sort((a, b)=>{return a.rating - b.rating});
				this.yelpMostRecentReviews = this.yelpReviews.concat().sort((a, b)=>{if (b.time_created == a.time_created) return 0; else if (b.time_created > a.time_created) return 1; else return -1;});
				this.yelpLeastRecentReviews = this.yelpReviews.concat().sort((a, b)=>{if (a.time_created == b.time_created) return 0; else if (a.time_created > b.time_created) return 1; else return -1;});
			}
    }, error => {
    	this.yelpFail = true;
    });
  }
  highestRatingCmp(a, b) {
  	return a.rating - b.rating;
  }
  lowestRatingCmp(a, b) {
  	return b.rating - a.rating;
  }
  mostRecentCmp(a, b) {
  	return a.unix - b.unix;
  }
  leastRecentCmp(a, b) {
  	return b.unix - a.unix;
  }
  clickGoogle(){
  	this.sourceSelected = "Google Reviews";
  	this.sourceClicked = "google";
  }
  clickYelp(){
  	this.sourceSelected = "Yelp Reviews";
  	this.sourceClicked = "yelp";
  }
  clickDefault() {
    this.orderSelected = "Default Order";
    this.reviews = this.googleDefaultReviews;
    this.yelpReviews = this.yelpDefaultReviews;
    this.ref.markForCheck();
    this.ref.detectChanges();
  }
  clickHighestRating(){
    this.orderSelected = "Highest Rating";
    this.reviews = this.googleHighestRatingReviews;
    this.yelpReviews = this.yelpHighestRatingReviews;
    this.ref.markForCheck();
    this.ref.detectChanges();
		// console.log(this.googleHighestRatingReviews[0].rating +" "+ this.googleHighestRatingReviews[1].rating+" "+this.googleHighestRatingReviews[2].rating+" "+this.googleHighestRatingReviews[3].rating+" "+this.googleHighestRatingReviews[4].rating)
		// console.log(this.reviews[0].rating +" "+ this.reviews[1].rating+" "+this.reviews[2].rating+" "+this.reviews[3].rating+" "+this.reviews[4].rating);
  }
  clickLowestRating() {
    this.orderSelected = "Lowest Rating";
    this.reviews = this.googleLowestRatingReviews;
    this.yelpReviews = this.yelpLowestRatingReviews;
    this.ref.markForCheck();
    this.ref.detectChanges();
		// console.log(this.googleLowestRatingReviews[0].rating +" "+ this.googleLowestRatingReviews[1].rating+" "+this.googleLowestRatingReviews[2].rating+" "+this.googleLowestRatingReviews[3].rating+" "+this.googleLowestRatingReviews[4].rating)
		// console.log(this.reviews[0].rating +" "+ this.reviews[1].rating+" "+this.reviews[2].rating+" "+this.reviews[3].rating+" "+this.reviews[4].rating);
  }
  clickMostRecent(){
    this.orderSelected = "Most Recent";
    this.reviews = this.googleMostRecentReviews;
    this.yelpReviews = this.yelpMostRecentReviews;
    this.ref.markForCheck();
    this.ref.detectChanges();
  }
  clickLeastRecent(){
    this.orderSelected = "Least Recent";
    this.reviews = this.googleLeastRecentReviews;
    this.yelpReviews = this.yelpLeastRecentReviews;
    this.ref.markForCheck();
    this.ref.detectChanges();
  }
  clickStreet() {
  	this.mapDisplay = false;
  }
  clickMap() {
  	this.mapDisplay = true;
  	this.ref.markForCheck();
  }
  clickFavorite() {
  	this.place.favorite = !this.place.favorite;
    if (this.place.favorite) {
      // this.favoriteArr.push(this.place);
      this.place.addtime = moment().utc().format();
      let string = JSON.stringify(this.place);
      window.localStorage[this.place.placeID] = string;
    }
    else {
      // for (let i = 0; i < this.favoriteArr.length; i++) {
      //   if (this.favoriteArr[i].placeID == this.place.placeID) {
      //     this.favoriteArr.splice(i, 1);
      //   }
      // }
      window.localStorage.removeItem(this.place.placeID);
      this.favDeleteEmitter.emit(this.place);
    }
    // this.allPlacesEmitter.emit(this.allPlaces);
    // this.favorite.emit(this.favoriteArr);
    // this.places[place.num-1].favorite = !this.places[place.num-1].favorite;
    // console.log(this.places);
  }
  clickTwitter() {
  	let url = "";
	if (this.placeData.website != undefined) {
		url = this.placeData.website;
	}
	else {
		url = this.placeData.url;
	}
  	let URL = "https://twitter.com/intent/tweet?text="+this.text+"&url="+encodeURIComponent(url)+"&hashtags=TravelAndEntertainmentSearch";
  	let windowHeight = 500;
  	let windowWidth = 600;
  	let windowName = "";
    let centerLeft = (screen.width/2)-(windowWidth/2);
    let centerTop = (screen.height/2)-(windowHeight/2);
    let windowFeatures = 'toolbar=no, location=no, directories=no, status=no, menubar=no, titlebar=no, scrollbars=no, resizable=no, ';
    let opener = window.open(URL, windowName, windowFeatures +' width='+ windowWidth +', height='+ windowHeight +', top='+ centerTop +', left='+ centerLeft);
    // (<HTMLElement>opener.document.getElementsByTagName("title")[0]).innerText = "Share a link on Twitter";
    return opener;
  }
  clickOpenHours() {
  	this.openHoursEmitter.emit(this.placeData.hours);
  }
  // clickHours() {
  // 	this.hoursDisplay = true;
  // 	this.ref.markForCheck();
  //   this.ref.detectChanges();
  // }
  // closeHours() {
  // 	this.hoursDisplay = false;
  // 	this.ref.markForCheck();
  //   this.ref.detectChanges();
  // }
  clickPhoto() {
  	this.selected='photo';
  	// this.bricklayer.breakpoint;
  	this.ref.markForCheck();
  	this.ref.detectChanges();
  }
  inputChange(formInput) {
  	if (formInput.value != "Your location") {
  		this.formInputChange = true;
  	}
  }
}
