package ge.geolab.bookswap.views.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import ge.geolab.bookswap.R;
import ge.geolab.bookswap.models.Book;
import ge.geolab.bookswap.models.GroupItem;
import ge.geolab.bookswap.views.customViews.AnimatedExpandableListView;

/**
 * Created by dalkh on 31-Jan-16.
 */
public class ExpandableListAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {
    private LayoutInflater inflater;

    private List<GroupItem> items;

    public ExpandableListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setData(List<GroupItem> items) {
        this.items = items;
    }

    @Override
    public Book getChild(int groupPosition, int childPosition) {
        List<Book> childList=items.get(groupPosition).getItems();
        return childList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder holder;
        Book item = getChild(groupPosition, childPosition);
        if (convertView == null) {
            holder = new ChildHolder();
            convertView = inflater.inflate(R.layout.expandable_list_child, parent, false);
            holder.title = (TextView) convertView.findViewById(R.id.offered_book_title);
            holder.owner = (TextView) convertView.findViewById(R.id.owner);
            convertView.setTag(holder);
        } else {
            holder = (ChildHolder) convertView.getTag();
        }


        final String[] username = {""};
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                item.getId(),
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
            /* handle the result */
                        Log.v("OfferBookActivity", response.toString());
                        try {
                            if(response.getJSONObject()!=null) {
                                username[0] = response.getJSONObject().getString("name");

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();


        holder.title.setText(item.getTitle());
        holder.owner.setText(username[0]);


        return convertView;
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        List<Book> childList=items.get(groupPosition).getItems();
        return childList.size();
    }

    @Override
    public GroupItem getGroup(int groupPosition) {
        return items.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return items.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder holder;
        GroupItem item = getGroup(groupPosition);
        if (convertView == null) {
            holder = new GroupHolder();
            convertView = inflater.inflate(R.layout.expandable_list_header, parent, false);
            holder.title = (TextView) convertView.findViewById(R.id.my_book_title);
            convertView.setTag(holder);
        } else {
            holder = (GroupHolder) convertView.getTag();
        }

        holder.title.setText(item.getTitle());

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int arg0, int arg1) {
        return true;
    }
    private static class ChildHolder {
        TextView title;
        TextView owner;
    }

    private static class GroupHolder {
        TextView title;
    }

}
