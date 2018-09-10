import { Component, OnInit, ViewChild, EventEmitter, Input, Output, ElementRef, NgZone, ChangeDetectorRef } from '@angular/core';
// import { PatternValidator } from '@angular/forms';
import { NgModule } from '@angular/core';
import { FormGroup, FormBuilder, FormControl, Validators, NgForm} from '@angular/forms';
import * as $ from 'jquery';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import {} from '@types/googlemaps';
import { MapsAPILoader } from '@agm/core';

declare var google: any;

@Component({
  selector: 'app-form',
  templateUrl: './form.component.html',
  styleUrls: ['./form.component.css']
})

export class FormComponent implements OnInit {

  @Output() search = new EventEmitter();
  @Output() progressDisplayEmitter = new EventEmitter();
  @Output() searchFail = new EventEmitter();
  @Output() formClearEmitter = new EventEmitter();

  categories = [
  	{ type: 'Default', value: 'default' },
    { type: 'Airport', value: 'airport' },
    { type: 'Amusement Park', value: 'amusement_park' },
    { type: 'Aquarium', value: 'aquarium' },
    { type: 'Art Gallery', value: 'art_gallery' },
    { type: 'Bakery', value: 'bakery' },
    { type: 'Bar', value: 'bar' },
    { type: 'Beauty Salon', value: 'beauty_salon' },
    { type: 'Bowling Alley', value: 'bowling_alley' },
    { type: 'Bus Station', value: 'bus_station' },
    { type: 'Cafe', value: 'cafe' },
    { type: 'Campground', value: 'campground' },
    { type: 'Car Rental', value: 'car_rental' },
    { type: 'Casino', value: 'casino' },
    { type: 'Lodging', value: 'lodging' },
    { type: 'Movie Theater', value: 'movie_theater' },
    { type: 'Museum', value: 'museum' },
    { type: 'Night Club', value: 'night_club' },
    { type: 'Park', value: 'park' },
    { type: 'Parking', value: 'parking' },
    { type: 'Restaurant', value: 'restaurant' },
    { type: 'Shopping Mall', value: 'shopping_mall' },
    { type: 'Stadium', value: 'stadium' },
    { type: 'Subway Station', value: 'subway_station' },
    { type: 'Taxi Stand', value: 'taxi_stand' },
    { type: 'Train Station', value: 'train_station' },
    { type: 'Trainsit Station', value: 'trainsit_station' },
    { type: 'Travel Agency', value: 'travel_station' },
    { type: 'Zoo', value: 'zoo' }
  ];

  hereClicked=true;
  category:string = 'default';
  distance:string = '';
  fromradio = 'here';
  getHere = false;
  locationValue="";
  keywordValue="";

  lat = '';
  lng = '';

  searchData;
  // formClear;

  @ViewChild('f') form: NgForm;
  // @ViewChild('location') input: ElementRef;

  httpOptions = {
	  headers: new HttpHeaders({
	    'Content-Type':  'application/json'
	  })
	};

  serverUrl = "http://aobolihw8-env.us-west-1.elasticbeanstalk.com";

  // public latitude: number;
  // public longitude: number;
  // public searchControl: FormControl;
  // public zoom: number;
  // @ViewChild("keyword")
  // public searchElementRef: ElementRef;

  constructor(
    private http: HttpClient, 
    // private mapsAPILoader: MapsAPILoader,
    // private ngZone: NgZone
    private ref: ChangeDetectorRef
    ) { }

  ngOnInit(){ 
    this.hereClicked=true;
    this.category = 'default';
    this.distance = '';
    this.fromradio = 'here';
    this.getHere = false;
    this.locationValue="";
    this.keywordValue="";
    // this.formClear=false;
  	this.http.get("http://ip-api.com/json").subscribe(data => {
  		// this.IPdata = data.json();
  		this.lat = data['lat'];
  		this.lng = data['lon'];
  		if (this.lat!='' && this.lng!='') this.getHere = true;
  		// console.log(data);
  	});

  	// var input = document.getElementById('location');
  	var input1 = $("#location")[0];
    var input2 = $("#keyword")[0];
    // var input = this.input;
		var options = {};
		var locationAutocomplete = new google.maps.places.Autocomplete(input1, options);
    var keywordAutocomplete = new google.maps.places.Autocomplete(input2, options);
    // //set google maps defaults
    // this.zoom = 4;
    // this.latitude = 39.8282;
    // this.longitude = -98.5795;

    // //create search FormControl
    // this.searchControl = new FormControl();
    // //set current position
    // this.setCurrentPosition();
    // //load Places Autocomplete
    // this.mapsAPILoader.load().then(() => {
    //   let autocomplete = new google.maps.places.Autocomplete(this.searchElementRef.nativeElement, {
    //     types: ["address"]
    //   });
    //   autocomplete.addListener("place_changed", () => {
    //     this.ngZone.run(() => {
    //       //get the place result
    //       let place: google.maps.places.PlaceResult = autocomplete.getPlace();

    //       //verify result
    //       if (place.geometry === undefined || place.geometry === null) {
    //         return;
    //       }

    //       //set latitude, longitude and zoom
    //       this.latitude = place.geometry.location.lat();
    //       this.longitude = place.geometry.location.lng();
    //       this.zoom = 12;
    //     });
    //   });
    // });

  }

  // private setCurrentPosition() {
  //   if ("geolocation" in navigator) {
  //     navigator.geolocation.getCurrentPosition((position) => {
  //       this.latitude = position.coords.latitude;
  //       this.longitude = position.coords.longitude;
  //       this.zoom = 12;
  //     });
  //   }
  // }

  onFormSubmit(form: NgForm) {

    this.progressDisplayEmitter.emit(true);
  	// console.log(form);

  	var from = 'here';
  	var location = '';
  	if (this.fromradio == 'other') {
  		from = 'other';
  		// location = form.value.location;
  		location = (<HTMLInputElement>document.getElementById('location')).value;
  		// location = $('#location')[0].value;
  		// console.log($('#location')[0].value);
  		// location = this.location;
  	}

  	// console.log(this.fromradio);
  	// console.log(form.value.location);

    var distance = "10";
    // if (form.value.distance != "") distance = form.value.distance;
    if ((<HTMLInputElement>document.getElementById('distance')).value != "") distance = (<HTMLInputElement>document.getElementById('distance')).value;
    console.log("category"+form.value.category);
  	var params = new HttpParams()
	    .set('keyword', (<HTMLInputElement>document.getElementById('keyword')).value)
	    .set('category', form.value.category)
	    .set('distance', distance)
	    .set('from', from)
	    .set('lat', this.lat)
	    .set('lng', this.lng)
	    .set('location', location);

  	// console.log(data);
  	// console.log(params);

  	// this.http.get(this.serverUrl, {params});
  	// var res = this.http.get(this.serverUrl);
  	this.http.get(this.serverUrl+"/submit", {params}).subscribe(data => {
  		this.searchData = data;
      this.search.emit(this.searchData);
      this.searchFail.emit(false);
      this.progressDisplayEmitter.emit(false);
      this.formClearEmitter.emit(false);
  		// this.showSearchData();
  	}, error=>{
      this.searchFail.emit(true);
      this.progressDisplayEmitter.emit(false);
      this.formClearEmitter.emit(false);
    });
  	// console.log(this.searchData);
  	// this.http.get(this.serverUrl+"/submit", {params}).subscribe(data => this.searchData = {
  	// 	next_page_token : data['next_page_token'];
  	// });
  	// this.showSearchData();

  }

  showSearchData() {
  	console.log("form"+this.searchData);
  }

  clickHere(event: any) {
  	this.hereClicked = true;
  }

  clickOther(event: any) {
  	this.hereClicked = false;
  }

  resetForm() {
  	this.form.reset();
  	//clear others
    // this.hereClicked=true;
    // this.category = 'default';
    // this.distance = '10';
    // this.fromradio = 'here';
    // this.getHere = false;
    // this.keywordValue="";
    // this.locationValue="";
    // this.ref.markForCheck();
    // this.ref.detectChanges();
    // (<HTMLInputElement>document.getElementById("distance")).value="10";
    (<HTMLSelectElement>document.getElementById("category")).options[0].selected=true;
    (<HTMLInputElement>document.getElementById("here")).checked=true;
    // (<HTMLInputElement>document.getElementById("location")).disabled=true;
    this.hereClicked = true;
    this.formClearEmitter.emit(true);
  }

}

// export interface searchData {
//   next_page_token: string;
// }

