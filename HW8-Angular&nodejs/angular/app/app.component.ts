import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
	
  title = 'app';
  searchData;
  detailsSelected : boolean = false;
  // place;
  progressDisplay;
  searchFail;
  formClear=false;

  constructor() {
    this.progressDisplay = false;
  }


  search(data) {
  	this.searchData = data;
  	// console.log("app"+this.searchData);
  }
  // getDetails(detailsSelected : boolean) {
  // 	this.detailsSelected = detailsSelected;
  //   // console.log("app detailsSelected"+detailsSelected);
  // }
  // getDetailPlace(placeID) {
  // 	this.placeID = placeID;
  // 	// console.log("app placeID"+placeID);
  // }

  getProgressDisplay(progressDisplay) {
    this.progressDisplay = progressDisplay;
  }
  getSearchFail(flag) {
    this.searchFail = flag;
  }
  getFormClear(flag) {
    this.formClear = flag;
  }
}
