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
    private static final boolean DEBUG = false;
    private static final int NOTIFICATION_LOADING_ALERT = 1;
    // the fragment's state
    private ResearchTree mResearch = null;
    @SuppressWarnings("FieldCanBeLocal")
    private ResearchListAdapter mAdapter = null;
    private OnResearchListFragmentInteractionListener mListener;

    public ResearchListFragment() {
        if (DEBUG)
            debug("ResearchListFragment", "> ResearchListFragment()");
        if (DEBUG)
            debug("ResearchListFragment", "< ResearchListFragment()");
    }

    private static void debug(String tag, String msg) {
        System.err.println(tag + ": " + msg);
        Log.d(tag, msg);
    }

    /**
     * Use this factory method to create a new instance of this fragment
     * using the provided parameters.
     *
     * @return A new instance of fragment ResearchListFragment.
     */
    public static ResearchListFragment newInstance() {
        if (DEBUG) debug("ResearchListFragment", "> newInstance()");
        //noinspection UnnecessaryLocalVariable
        ResearchListFragment fragment = new ResearchListFragment();
//        Bundle args = new Bundle();
//        args.putSerializable(ARG_RESEARCH, research);
//        fragment.setArguments(args);
        if (DEBUG)
            debug("ResearchListFragment", "< newInstance() returning " + fragment);
        return fragment;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG)
            debug("ResearchListFragment", "> onCreate(" + savedInstanceState + ")");
        try {
            this.mResearch = ResearchTree.loadXmlResearchTree(this.getResources().getXml(R.xml.default_research_tree));
        } catch (IOException e) {
            this.mResearch = new ResearchTree();
            NotificationCompat.Builder alert = new NotificationCompat.Builder(this.getContext()).setSmallIcon(android.R.drawable.ic_dialog_alert).setContentTitle("Unable to load default research tree").setContentText(e.getLocalizedMessage());
            ((NotificationManager)this.getContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_LOADING_ALERT, alert.build());
        }
        if (DEBUG)
            debug("ResearchListFragment", "< onCreate(" + savedInstanceState + ")");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (DEBUG)
            debug("ResearchListFragment", "> onCreateView(" + inflater + ", " + container + ", " + savedInstanceState + ")");
        View rootView = inflater.inflate(R.layout.fragment_research, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.research_list_view);
        this.mAdapter = new ResearchListAdapter(this.getActivity(), this.mResearch);
        listView.setAdapter(this.mAdapter);
        if (DEBUG)
            debug("ResearchListFragment", "< onCreateView(" + inflater + ", " + container + ", " + savedInstanceState + ") returning " + rootView);
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
        if (DEBUG)
            debug("ResearchListFragment", "> onAttach(" + context + ")");
        if (context instanceof OnResearchListFragmentInteractionListener) {
            mListener = (OnResearchListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnResearchListFragmentInteractionListener");
        }
        if (DEBUG)
            debug("ResearchListFragment", "< onAttach(" + context + ")");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (DEBUG) debug("ResearchListFragment", "> onDetach()");
        mListener = null;
        if (DEBUG) debug("ResearchListFragment", "< onDetach()");
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
