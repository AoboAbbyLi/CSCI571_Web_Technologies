import { Component, Input, OnChanges, SimpleChange, EventEmitter, Output, ChangeDetectorRef } from '@angular/core';
import * as moment from 'moment';

@Component({
  selector: 'app-favorites',
  templateUrl: './favorites.component.html',
  styleUrls: ['./favorites.component.css']
})
export class FavoritesComponent implements OnChanges {

	// @Input() favoriteArr;
	// @Input() favoriteChange;
  @Input() searchFail;
	@Input() allPlaces;
	@Input() resultsSelected;
	@Input() placeSelected;
  @Input() detailsDeletePlace;
	@Output() details = new EventEmitter();
  @Output() detailPlace = new EventEmitter();
  // @Output() favorite = new EventEmitter();
  @Output() allPlacesEmitter = new EventEmitter();
  @Output() noRecordsEmitter = new EventEmitter();
  localStorage;
  favoriteData;
  favoritePage;
  endIndex;
  // currPage;
  // hasPreviousPage;
  hasNoRecords : boolean = false;

  constructor(
  	private ref: ChangeDetectorRef
  	) { }

  ngOnChanges(changes: {[propKey: string]: SimpleChange}) {

    if (changes["allPlaces"] || changes["resultsSelected"] || changes["searchFail"]) {
      this.favoriteData = Array();
      this.localStorage = window.localStorage;
      if (this.localStorage.length == 0) {
        this.hasNoRecords = true;
        this.noRecordsEmitter.emit(true);
      }
      else {
        this.hasNoRecords = false;
        this.noRecordsEmitter.emit(false);
      }
      let arr = new Array();
      for (let i = 0; i < this.localStorage.length; i++) {
        let key = this.localStorage.key(i);
        let value = JSON.parse(this.localStorage[key]);
        arr.push(value);
      }
      this.favoriteData = arr.concat().sort((a, b) => {
        return moment(a.addtime).isBefore(moment(b.addtime))?-1:1;
      });

      this.favoritePage = this.favoriteData.slice(0, 20);
      this.endIndex = this.favoritePage.length - 1;
      // console.log(this.endIndex);
    console.log(this.favoriteData.length);
      // this.hasPreviousPage = false;
      // this.currPage = 1;
      // this.favoriteData = arr.concat();
      this.ref.markForCheck();
      this.ref.detectChanges();
      // console.log("favorite ngOnChanges");
    }
    else if (changes["detailsDeletePlace"]) {
      let deleteIndex = 0;
      for (let i = 0; i < this.favoriteData.length; i++) {
        if (this.favoriteData[i].placeID == this.detailsDeletePlace.placeID) {
          deleteIndex = i;
          console.log("deleteIndex"+deleteIndex);
          break;
        }
      }
      console.log("endIndex"+this.endIndex);
      if (deleteIndex <= this.endIndex) {
        this.endIndex = Math.min(this.endIndex, this.favoriteData.length-2);
        console.log("update endIndex"+this.endIndex);
      }
      this.favoriteData = Array();
      this.localStorage = window.localStorage;
      if (this.localStorage.length == 0) {
        this.hasNoRecords = true;
        this.noRecordsEmitter.emit(true);
      }
      else {
        this.hasNoRecords = false;
        this.noRecordsEmitter.emit(false);
      }
      let arr = new Array();
      for (let i = 0; i < this.localStorage.length; i++) {
        let key = this.localStorage.key(i);
        let value = JSON.parse(this.localStorage[key]);
        arr.push(value);
      }
      this.favoriteData = arr.concat().sort((a, b) => {
        return moment(a.addtime).isBefore(moment(b.addtime))?-1:1;
      });
      this.favoritePage = this.favoriteData.slice(Math.floor(this.endIndex/20)*20, this.endIndex+1);
      console.log("start"+(Math.floor(this.endIndex/20)*20));
      console.log("data"+this.favoriteData.length);
      console.log("page"+this.favoritePage.length);
    }
  	
  }

  clickDelete(place, index) {
    // for (let i = 0; i < this.favoriteArr.length; i++) {
    //   if (this.favoriteArr[i].placeID == place.placeID) {
    //     this.favoriteArr.splice(i, 1);
    //   }
    // }
    window.localStorage.removeItem(place.placeID);
    if (this.allPlaces) {
      for (let i = 0; i < this.allPlaces.length; i++) {
        for (let j = 0; j < this.allPlaces[i].length; j++) {
          if (place.placeID == this.allPlaces[i][j].placeID) {
            this.allPlaces[i][j].favorite = false;
          }
        }
      }
    }
    console.log("data splice index"+(this.endIndex-this.favoritePage.length+index+1));
    this.favoriteData.splice(this.endIndex-this.favoritePage.length+index+1, 1);
    this.favoritePage.splice(index, 1);
    console.log("splice length"+this.favoritePage.length);
    console.log("endIndex"+this.endIndex+"data length"+this.favoriteData.length);

    if (this.endIndex <= this.favoriteData.length-1) {
      console.log("data slice"+this.favoriteData.slice(this.endIndex, this.endIndex+1)[0].placeID);
      this.favoritePage.push(this.favoriteData.slice(this.endIndex, this.endIndex+1)[0]);

    }
    else {
      this.endIndex--;
      if (this.favoritePage.length == 0) {
            var pageLength = this.favoritePage.length;
            this.favoritePage = this.favoriteData.slice(this.endIndex-19, this.endIndex+1);
      }
    }
    // console.log("page length"+this.favoritePage.length);
    // console.log("index"+this.endIndex);
  	// this.favorite.emit(this.favoriteArr);
  	this.allPlacesEmitter.emit(this.allPlaces);
  	// this.ref.markForCheck();
   //  this.ref.detectChanges();
   // console.log("this.placeSelected.placeID"+this.placeSelected.placeID);
   //  console.log("place.placeID"+place.placeID);
   if (this.placeSelected && this.placeSelected.placeID == place.placeID) {
     place.favorite = !place.favorite;
     this.detailPlace.emit(place);
   }
   if (window.localStorage.length == 0) {
     this.hasNoRecords = true;
     this.noRecordsEmitter.emit(true);
     // this.ref.markForCheck();
     // this.ref.detectChanges();
   }
   // console.log("Page19"+this.favoritePage[19].placeID);
   this.ref.markForCheck();
   this.ref.detectChanges();
  }

  clickDetails(place) {
    this.placeSelected = place;
    this.details.emit(true);
    this.detailPlace.emit(place);
  }

  clickNext() {
    this.favoritePage = this.favoriteData.slice(this.endIndex+1, this.endIndex+21);
    console.log("length"+this.favoritePage.length);
    this.endIndex = this.endIndex + Math.min(20, this.favoritePage.length);
    console.log("clickNext"+this.endIndex);
    this.ref.markForCheck();
    this.ref.detectChanges();
  }
  clickPrevious() {
    var pageLength = this.favoritePage.length;
    this.endIndex -= pageLength;
    console.log("clickPrevious"+this.endIndex);
    this.favoritePage = this.favoriteData.slice(this.endIndex-19, this.endIndex+1);
    console.log("length"+this.favoritePage.length);
    this.ref.markForCheck();
    this.ref.detectChanges();
  }
}
