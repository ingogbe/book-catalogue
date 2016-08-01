package com.ingoguilherme.cadastrolivros;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BookInformationFragment extends Fragment {
    private static final String ARG_PARAM1 = "isbn";
    private static final String ARG_PARAM2 = "title";
    private static final String ARG_PARAM3 = "authors";
    private static final String ARG_PARAM4 = "publishedDate";
    private static final String ARG_PARAM5 = "description";

    private String isbn;
    private String title;
    private String[] authors;
    private String publishedDate;
    private String description;

    private OnBookInformationFragmentInteractionListener mListener;

    public BookInformationFragment() {
        // Required empty public constructor
    }

    public static BookInformationFragment newInstance(String isbn, String title, String[] authors, String publishedDate, String description) {
        BookInformationFragment fragment = new BookInformationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, isbn);
        args.putString(ARG_PARAM2, title);
        args.putStringArray(ARG_PARAM3, authors);
        args.putString(ARG_PARAM4, publishedDate);
        args.putString(ARG_PARAM5, description);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.isbn = getArguments().getString(ARG_PARAM1);
            this.title = getArguments().getString(ARG_PARAM2);
            this.authors = getArguments().getStringArray(ARG_PARAM3);
            this.publishedDate = getArguments().getString(ARG_PARAM4);
            this.description = getArguments().getString(ARG_PARAM5);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_book_information, container, false);

        ((TextView) rootView.findViewById(R.id.tvIsbn)).setText(isbn);
        ((TextView) rootView.findViewById(R.id.tvTitle)).setText("Title: " + title);

        String authorsS = "";
        for(int i = 0; i < authors.length; i++)
            if(i == 0)
                authorsS = authors[i] + "; ";
            else
                authorsS = authorsS + authors[i] + "; ";

        ((TextView) rootView.findViewById(R.id.tvAuthors)).setText("Authors: " + authorsS);
        ((TextView) rootView.findViewById(R.id.tvPublishedDate)).setText("Published Date: " + publishedDate);
        ((TextView) rootView.findViewById(R.id.tvDescription)).setText("Description: " + description);


        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onBookInformationFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBookInformationFragmentInteractionListener) {
            mListener = (OnBookInformationFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnBookInformationFragmentInteractionListener {
        void onBookInformationFragmentInteraction(Uri uri);
    }
}
