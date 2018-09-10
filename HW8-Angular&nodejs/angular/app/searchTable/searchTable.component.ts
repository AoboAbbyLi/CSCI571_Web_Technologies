import { Component, Input, OnChanges, SimpleChange, EventEmitter, Output,ChangeDetectorRef } from '@angular/core';
import { trigger ,state, style, animate, transition} from '@angular/animations';

@Component({
	selector: 'app-searchtable',
	templateUrl: './searchTable.component.html',
	styleUrls: ['./searchTable.component.css'],
	animations: [
	    trigger('detailsAnimation', [
	      state('true' , style({ opacity: 1, transform: 'translateX(0)' })),
	      state('false' , style({ opacity: 1, transform: 'translateX(0)' })),
	      // state('false', style({ opacity: 0, transform: 'scale(0.0)' })),
	      transition('false => true', [
		      style({transform: 'translateX(-100%)'}),
		      animate(500)
		    ]),
	      // transition('true => false', animate('300ms')),
	    ]),
	    trigger('listAnimation', [
	      state('true' , style({ opacity: 1, transform: 'translateX(0)' })),
	      state('false' , style({ opacity: 1, transform: 'translateX(0)' })),
	      // state('false', style({ opacity: 0, transform: 'scale(0.0)' })),
	      // transition 控制状态到状态以什么样的方式来进行转换
	      transition('false => true', [
		      style({transform: 'translateX(100%)'}),
		      animate(500)
		    ]),
	      // transition('true => false', animate('300ms')),
	    ])
  	]
})

export class searchTableComponent implements OnChanges {
	@Input() searchData;
	@Input() progressDisplay;
	@Input() searchFail;
	@Input() formClear;
	//@Output() searchDataOutput = new EventEmitter();
	// @Output() details = new EventEmitter();
	// @Output() detailPlace = new EventEmitter();

	detailsEnabled : boolean = false;
	detailsSelected : boolean = false;
	detailsClicked : boolean = false;
	resultsSelected : boolean = true;
	listClicked: boolean = false;
	formSubmitted = false;
	place = null;  //cannot clear
	placeSelected; //could clear
	// favoriteArr;
	allPlaces;
	resultsNoRecords : boolean = false;
	favNoRecords : boolean = false;
	favDeletePlace;
	openHours;

	constructor(private ref: ChangeDetectorRef){}

	ngOnChanges(changes: {[propKey: string]: SimpleChange}) {
		this.detailsEnabled = false;
		let dataProp = changes['searchData'];
		if (dataProp) {
			if (dataProp.isFirstChange()) {}
			else {
				this.formSubmitted = true;
				// this.favoriteArr = Array();
				this.allPlaces = Array();
				// this.place = null;
				this.placeSelected = null;
				// this.searchFail = false;
				this.detailsEnabled = false;
				this.detailsSelected = false;
				this.detailsClicked = false;
				this.resultsSelected = true;
				this.listClicked = false;
				this.resultsNoRecords = false;
				this.favNoRecords = false;
				this.ref.markForCheck();
				this.ref.detectChanges();
				// console.log("serachtable this.searchdata"+this.searchData);
			}
		}
		let clearProp = changes['formClear'];
		if (clearProp) {
			if (this.formClear == true) {
				this.formSubmitted = false;
				// this.favoriteArr = Array();
				// this.place = null;
				this.placeSelected = null;
				this.allPlaces = Array();
				// this.searchFail = false;
				this.detailsEnabled = false;
				this.detailsSelected = false;
				this.detailsClicked = false;
				this.resultsSelected = true;
				this.listClicked = false;
				this.resultsNoRecords = false;
				this.favNoRecords = false;
				this.ref.markForCheck();
				this.ref.detectChanges();
			}
		}
		
	}
	clickList(listSelected : boolean) {
		this.detailsSelected = !listSelected;
		this.detailsClicked = false;
		this.listClicked = true;
	}
	clickDetails() {
		this.detailsSelected = true;
		this.detailsClicked = true;
		this.listClicked = false;
	}
	clickResults() {
		this.resultsSelected = true;
		this.detailsClicked = false;
		this.detailsSelected = false;
	}
	clickFavorites() {
		this.resultsSelected = false;
		// console.log(window.localStorage);
		// this.ref.markForCheck();
  //   	this.ref.detectChanges();
		this.detailsClicked = false;
		this.detailsSelected = false;
	}
	getDetails(detailsSelected) {
		this.detailsSelected = detailsSelected;
		// this.details.emit(this.detailsSelected);
		this.detailsEnabled = true;
		this.listClicked = false;
	}
	getDetailPlace(place) {
		this.place = place;
		this.placeSelected = place;
		// console.log("searchTable place changes");
		// this.detailPlace.emit(placeID);
	}
	// getFavoriteArr(favoriteData) {
	// 	this.favoriteArr = favoriteArr;
	// }
	getAllPlaces(allPlaces) {
		this.allPlaces = allPlaces;
		// this.ref.markForCheck();
  //   	this.ref.detectChanges();
	}
	getResultsNoRecords(flag) {
		this.resultsNoRecords = flag;
	}
	getFavNoRecords(flag) {
		this.favNoRecords = flag;
	}
	favDelete(place) {
		this.favDeletePlace = place;
	}
	getOpenHours(openHours) {
		this.openHours = openHours;
		this.ref.markForCheck();
		this.ref.detectChanges();
	}

}