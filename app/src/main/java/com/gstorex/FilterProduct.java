package com.gstorex;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterProduct extends Filter {
    private ArrayList<ModelProduct> filterList;
    private AdapterProductSeller adapter;

    public FilterProduct(AdapterProductSeller adapter, ArrayList<ModelProduct> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults results = new FilterResults();
        //validate data search query
        if (constraint != null && constraint.length() > 0) {
            //search filed not empty , searching something , perform search

            //change to upper case , to make insensitive
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelProduct> filteredModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                //check , search by ttitle and category
                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint) || filterList.get(i).getProductDescription().toUpperCase().contains(constraint) || filterList.get(i).getProductCategory().toUpperCase().contains(constraint)) {
                    //add filtered data to list
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        } else {
            //search filed  empty , not searching  return original//all/complete list

            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.productsList = (ArrayList<ModelProduct>) results.values;
//refresh adapter
        adapter.notifyDataSetChanged();
    }
}
