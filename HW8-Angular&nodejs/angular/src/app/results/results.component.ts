import { Component, Input, OnChanges, SimpleChange, EventEmitter, Output, ChangeDetectorRef } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import * as moment from 'moment';

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.css']
})
// export class ResultsComponent implements OnInit {
export class ResultsComponent implements OnChanges {

  @Input() searchData;
	// @Input() favoriteArr;
  @Input() allPlaces;
  @Input() placeSelected;
  @Output() details = new EventEmitter();
  @Output() detailPlace = new EventEmitter();
  @Output() favorite = new EventEmitter();
  @Output() allPlacesEmitter = new EventEmitter();
  @Output() noRecordsEmitter = new EventEmitter();

	hasData = false;
	next_page_token = '';
  isLastPage = true;
  previous_page : boolean;
	places;
  place;
  // allPlaces;
  currPlaces : number; //input
  serverUrl = "http://aobolihw8-env.us-west-1.elasticbeanstalk.com";

  hasNoRecords : boolean = false;

  constructor(private http: HttpClient,
    private ref: ChangeDetectorRef
    ) {}

  //handle the searchData from submit
  ngOnChanges(changes: {[propKey: string]: SimpleChange}) {
    console.log("results ngOnchange");
    if (changes.hasOwnProperty("searchData")) {
      this.previous_page = false;
      this.next_page_token = '';
      // this.allPlaces = Array();
      this.currPlaces = 0;
      this.isLastPage = true;
      // this.favoriteData = Array();
      // let log: string[] = [];
      // if (changes.hasOwnProperty('searchData')) {
        let dataProp = changes['searchData'];
        console.log("results dataProp"+dataProp);
        // let to = JSON.stringify(dataProp.currentValue);
        // if (dataProp.isFirstChange()) {
          // console.log(`Initial value of ${propName} set to ${to}`);
          // this.allPlaces = Array();
          // this.currPlaces = 0;
        // }
        // else {
         if (this.searchData) {
          console.log("results this.sesarchData"+this.searchData);
          // let from = JSON.stringify(dataProp.previousValue);
          this.hasData = true;
          // this.hasNoRecords = false;
          if (this.searchData.hasOwnProperty("next_page_token")) {
            this.next_page_token = this.searchData['next_page_token'];
          }
          else {
            this.next_page_token = '';
          }
          var places = Array();
          // if (this.searchData["results"].length == 0) this.hasNoRecords = true;
          if (this.searchData["results"].length == 0) {
            this.hasNoRecords = true;
            this.noRecordsEmitter.emit(true);
          }
          else {
            this.hasNoRecords = false;
            this.noRecordsEmitter.emit(false);
          }
          for (var i = 0; i < this.searchData["results"].length; i++) {
            var place : Place = {
              num : i+1,
              category: this.searchData['results'][i]['icon'],
              name: this.searchData['results'][i]['name'],
              address: this.searchData['results'][i]['vicinity'],
              favorite: window.localStorage[this.searchData['results'][i]['place_id']]?true:false,
              placeID: this.searchData['results'][i]['place_id'],
              lat: this.searchData['results'][i]['geometry']['location']['lat'],
              lng: this.searchData['results'][i]['geometry']['location']['lng']
            }
            places.push(place);
          }
          this.places = places;
          this.allPlaces.push(places);
          this.allPlacesEmitter.emit(this.allPlaces);
        }
          // console.log(`searchData changed from ${from} to ${to}`);
          // console.log('this.places'+this.places);
        // }
      // }
    }
    else {
      this.ref.markForCheck();
      this.ref.detectChanges();
    }
    
    // for (let propName in changes) {
    //   let changedProp = changes[propName];
    //   let to = JSON.stringify(changedProp.currentValue);
    //   if (changedProp.isFirstChange()) {
    //     // console.log(`Initial value of ${propName} set to ${to}`);
    //     this.allPlaces = Array();
    //     this.currPlaces = 0;
    //   } else {
    //     if (this.currPlaces == this.allPlaces.length) {
    //       let from = JSON.stringify(changedProp.previousValue);
    //       this.hasData = true;
    //       if (this.searchData.hasOwnProperty("next_page_token")) {
    //         this.next_page_token = this.searchData['next_page_token'];
    //       }
    //       var places = Array();
    //       for (var i = 0; i < this.searchData["results"].length; i++) {
    //         var place : Place = {
    //           num : i+1;
    //           category: this.searchData['results'][i]['icon'];
    //           name: this.searchData['results'][i]['name'];
    //           address: this.searchData['results'][i]['vicinity'];
    //           favorite: false;
    //           placeID: this.searchData['results'][i]['place_id'];
    //         }
    //         places.push(place);
    //       }
    //       this.places = places;
    //     }
    //     else {

    //     }
    //     console.log(`${propName} changed from ${from} to ${to}`);
    //   }
    // }
  }

  clickFavorite(place) {
    place.favorite = !place.favorite;
    if (place.favorite) {
      place.addtime = moment().utc().format();
      // this.favoriteArr.push(place);
      let string = JSON.stringify(place);
      window.localStorage.setItem(place.placeID, string);
    }
    else {
      // for (let i = 0; i < this.favoriteArr.length; i++) {
      //   if (this.favoriteArr[i].placeID == place.placeID) {
      //     this.favoriteArr.splice(i, 1);
      //   }
      // }
        window.localStorage.removeItem(place.placeID);
    }
    this.allPlacesEmitter.emit(this.allPlaces);
    //update star in the detail page
    // console.log("this.placeSelected.placeID"+this.placeSelected.placeID);
    // console.log("place.placeID"+place.placeID);
    if (this.placeSelected && this.placeSelected.placeID == place.placeID) {
      this.detailPlace.emit(place);
    }
    // this.ref.markForCheck();
    // this.ref.detectChanges();
    // this.favorite.emit(this.favoriteData);
    // this.places[place.num-1].favorite = !this.places[place.num-1].favorite;
    // console.log(this.places);
  }

  clickDetails(place) {
    this.placeSelected = place;
    this.details.emit(true);
    this.detailPlace.emit(place);
  }

  clickPrevious() {
    this.currPlaces--;
    this.places = this.allPlaces[this.currPlaces];
    if (this.currPlaces == 0) this.previous_page = false;
    this.isLastPage = false;
    // console.log(this.currPlaces);
    // console.log(this.places);
  }

  clickNext() {
    this.previous_page = true;
    if (this.currPlaces == this.allPlaces.length-1) {
      var params = new HttpParams()
        .set('next_page_token', this.next_page_token);
      this.http.get(this.serverUrl+"/next_page", {params}).subscribe(data => {
        this.searchData = data;
        this.updateData();
      });
      this.isLastPage = true;
    }
    else {
      this.currPlaces++;
      this.places = this.allPlaces[this.currPlaces];
      if (this.currPlaces == this.allPlaces.length-1) this.isLastPage = true;
      else this.isLastPage = false;
    }
    // console.log("allPlaces"+this.allPlaces);
  }
  updateData() {
    var places = Array();
    for (var i = 0; i < this.searchData["results"].length; i++) {
      var place : Place= {
        num : i+1,
        category: this.searchData['results'][i]['icon'],
        name: this.searchData['results'][i]['name'],
        address: this.searchData['results'][i]['vicinity'],
        favorite: false,
        placeID: this.searchData['results'][i]['place_id'],
        lat: this.searchData['results'][i]['geometry']['location']['lat'],
        lng: this.searchData['results'][i]['geometry']['location']['lng']
      }
      places.push(place);
    }
    // console.log("update places"+this.places);
    this.places = places;
    this.allPlaces.push(places);
    this.currPlaces++;
    if (this.searchData.hasOwnProperty("next_page_token")) {
      this.next_page_token = this.searchData['next_page_token'];
    }
    else {
      this.next_page_token = '';
    }
    this.allPlacesEmitter.emit(this.allPlaces);
  }

}

export class Place {
	num : number;
	category: string;
	name: string;
	address : string;
	favorite : boolean;
	placeID : string;
  lat: number;
  lng: number;
}
