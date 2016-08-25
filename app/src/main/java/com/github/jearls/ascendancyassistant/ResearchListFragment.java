package com.github.jearls.ascendancyassistant;

import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass. Activities that contain this
 * fragment must implement the {@link OnResearchListFragmentInteractionListener}
 * interface to handle interaction events. Use the {@link
 * ResearchListFragment#newInstance} factory method to create an
 * instance of this fragment.
 */
public class ResearchListFragment extends Fragment {
    private static final int NOTIFICATION_LOADING_ALERT = 1;
    // the fragment's state
    private ResearchTree mResearch = null;
    @SuppressWarnings("FieldCanBeLocal")
    private ResearchListAdapter mAdapter = null;

    private OnResearchListFragmentInteractionListener mListener;

    public ResearchListFragment() {
        Log.d("ResearchListFragment", "ResearchListFragment()");
    }

    /**
     * Use this factory method to create a new instance of this fragment
     * using the provided parameters.
     *
     * @return A new instance of fragment ResearchListFragment.
     */
    public static ResearchListFragment newInstance() {
        Log.d("ResearchListFragment", "newInstance()");
        //noinspection UnnecessaryLocalVariable
        ResearchListFragment fragment = new ResearchListFragment();
//        Bundle args = new Bundle();
//        args.putSerializable(ARG_RESEARCH, research);
//        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ResearchListFragment", "onCreate()");
        try {
            this.mResearch = ResearchTree.loadXmlResearchTree(this.getResources().getXml(R.xml.default_research_tree));
        } catch (IOException e) {
            this.mResearch = new ResearchTree();
            NotificationCompat.Builder alert = new NotificationCompat.Builder(this.getContext()).setSmallIcon(android.R.drawable.ic_dialog_alert).setContentTitle("Unable to load default research tree").setContentText(e.getLocalizedMessage());
            ((NotificationManager)this.getContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_LOADING_ALERT, alert.build());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("ResearchListFragment", "onCreateView()");
        View rootView = inflater.inflate(R.layout.fragment_research, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.research_list_view);
        this.mAdapter = new ResearchListAdapter(this.getActivity(), this.mResearch);
        listView.setAdapter(this.mAdapter);
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    @SuppressWarnings("unused")
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onResearchListFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("ResearchListFragment", "onAttach()");
        if (context instanceof OnResearchListFragmentInteractionListener) {
            mListener = (OnResearchListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnResearchListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("ResearchListFragment", "onDetach()");
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain
     * this fragment to allow an interaction in this fragment to be
     * communicated to the activity and potentially other fragments
     * contained in that activity.
     * <p/>
     * See the Android Training lesson <a href= "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnResearchListFragmentInteractionListener {
        // TODO: Update argument type and sName
        void onResearchListFragmentInteraction(Uri uri);
    }
}
