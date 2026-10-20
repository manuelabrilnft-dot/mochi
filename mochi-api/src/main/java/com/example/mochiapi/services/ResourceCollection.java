package com.example.mochiapi.services;
import java.util.List;
//Not in use right now
//Will be used if pagination is needed
public class ResourceCollection<T> {
    private List<T> items;
    private int totalCount;
    private int offset;
    private int limit;

    public ResourceCollection() {
    }

    public ResourceCollection(List<T> items) {
        this.items = items;
        this.totalCount = items.size();
    }

    public ResourceCollection(List<T> items, int offset, int limit) {
        this.totalCount = items.size();
        this.items = items;
        this.offset = offset;
        this.limit = limit;
    }

    public void paginate(){
        this.items = items.stream()
                .skip(offset)
                .limit(limit)
                .toList();
    }

    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
}