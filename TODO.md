# A working tabulation of items that still need to get fixed for the next release of the library (TBD)

* Logcat reports a warning with the demo application about only being able to 
  request a single permission at a time. A suggested fix, thereby chaining a sequential 
  list of permission requests on after another as they gradually return, is 
  documented [here](https://stackoverflow.com/questions/42035244/getting-w-activity-can-request-only-one-set-of-permissions-at-a-time).
* The gradient factory new instance / static generate methods are not returning the 
  correctly formatted Drawable instances. This is lower priority, but still needs to get tracked down. 
* The library Activity toolbar navigation buttons to select new starting points for file trees 
  by stanard path-type locations are not currently getting populated and then displayed. 
  Right now, the LinearLayout does not expand to show the inflated drawable icons (though the 
  onClick handlers for these buttons are presumably there if we could see the buttons 
  to click on them). 
* Need to set (reset) the customized, built-in file types versus folder (directory type) icons in the 
  file items lists displayed in the RecyclerView.
* The LHS FileType, file items layout displays have a CheckBox to select (if allowed) that file and/or directory 
  entry to get returned to the client code. It currently is not working correctly.
* **TODO:** Continue forward updating the sum of RecyclerView interfaces to only load a small sub-block of the 
  full folder files list at a time. Right now, while the display is correctky listing out the directory 
  contents, loading a new directory is so slow and sluggish that the library is not yet useful for production cases ...
