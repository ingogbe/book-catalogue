package com.ingoguilherme.cadastrolivros;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ingoguilherme.cadastrolivros.barcode.reader.zxing.IntentIntegrator;
import com.ingoguilherme.cadastrolivros.barcode.reader.zxing.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class BarcodeFragment extends Fragment {

    private OnBarcodeFragmentInteractionListener mListener;

    public BarcodeFragment() {
        // Required empty public constructor
    }

    public static BarcodeFragment newInstance() {
        BarcodeFragment fragment = new BarcodeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentIntegrator integrator = IntentIntegrator.forFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt(getResources().getString(R.string.barcode_scan));
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(resultCode == getActivity().RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

            if (scanResult != null) {
                Log.d("BAR_CODE", scanResult.getContents());
                Log.d("BAR_CODE", scanResult.getFormatName());

                getBookInfo(scanResult.getContents());

            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_barcode, container, false);



        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onBarcodeFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBarcodeFragmentInteractionListener) {
            mListener = (OnBarcodeFragmentInteractionListener) context;
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

    public interface OnBarcodeFragmentInteractionListener {
        void onBarcodeFragmentInteraction(Uri uri);
    }

    public void getBookInfo(String isbn) {

        String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;
        String title = "";
        String authorsS[] = null;
        String publishedDate = "";
        String description = "";

        try {
            String page = new Communicator().executeHttpGet(url);
            JSONObject entry = new JSONObject(page);

            if(entry.getInt("totalItems") != 0) {
                JSONArray items = entry.getJSONArray("items");
                for (int j = 0; j < items.length(); j++) {
                    JSONObject item = (JSONObject) items.get(j);
                    JSONObject volumeInfo = item.getJSONObject("volumeInfo");

                    title = volumeInfo.getString("title");

                    JSONArray authors = volumeInfo.getJSONArray("authors");
                    authorsS = new String[authors.length()];

                    for (int i = 0; i < authors.length(); i++) {
                        String author = authors.getString(i);
                        authorsS[i] = author;
                    }

                    publishedDate = volumeInfo.getString("publishedDate");
                    description = volumeInfo.getString("description");

                    boolean foi = Conexao.conectar("book_catalogue","userFull","userFull");

                    if(foi) {
                        int[] idAuthors = new int[authorsS.length];

                        for (int i = 0; i < authorsS.length; i++) {
                            PreparedStatement pss = Conexao.prepareStatement("select id from author where name = ?");
                            pss.setString(1, authorsS[i]);
                            ResultSet rs = pss.executeQuery();
                            while(rs.next())
                                idAuthors[i] = rs.getInt("id");

                            if(idAuthors[i] <= 0) {

                                PreparedStatement ps = Conexao.prepareStatement("insert into author (name) values (?)");
                                ps.setString(1, authorsS[i]);
                                ps.executeUpdate();

                                PreparedStatement psss = Conexao.prepareStatement("select id from author where name = ?");
                                pss.setString(1, authorsS[i]);
                                ResultSet rss = pss.executeQuery();
                                rss.next();
                                idAuthors[i] = rss.getInt("id");
                            }
                        }

                        PreparedStatement ps2 = Conexao.prepareStatement("select id from book where isbn = ?");
                        ps2.setString(1, isbn);
                        ResultSet rs = ps2.executeQuery();
                        int idBook = -1;
                        while(rs.next())
                            idBook = rs.getInt("id");

                        if(idBook <= 0) {
                            PreparedStatement ps1 = Conexao.prepareStatement("insert into book (isbn, title, publisheddate, description) values (?, ?, ?, ?)");
                            ps1.setString(1, isbn);
                            ps1.setString(2, title);
                            ps1.setString(3, publishedDate);
                            ps1.setString(4, description);
                            ps1.executeUpdate();

                            PreparedStatement ps22 = Conexao.prepareStatement("select id from book where isbn = ?");
                            ps22.setString(1, isbn);
                            ResultSet rss = ps2.executeQuery();
                            rss.next();
                            idBook = rss.getInt("id");

                            for (int i = 0; i < authorsS.length; i++) {
                                PreparedStatement ps = Conexao.prepareStatement("insert into authorBook (idAuthor, idBook) values (?, ?)");
                                ps.setInt(1, idAuthors[i]);
                                ps.setInt(2, idBook);
                                ps.executeUpdate();
                            }
                        }

                        Conexao.fechar();
                    }


                    BookInformationFragment f = BookInformationFragment.newInstance(isbn, title, authorsS, publishedDate, description);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.your_placeholder, f);
                    ft.commit();

                    break;
                }
            }
            else{
                String[] authors = new String[1];
                authors[0] = "-";
                BookInformationFragment f = BookInformationFragment.newInstance("Não encontrado!", "-", authors, "-", "Livro não existe na base do Google");
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.your_placeholder, f);
                ft.commit();
            }

        } catch (JSONException e) {
            Log.d("CONEXAO",e.getMessage());
            String[] authors = new String[1];
            authors[0] = "-";
            BookInformationFragment f = BookInformationFragment.newInstance("Não encontrado!", "-", authors, "-", "Livro não existe na base do Google");
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.your_placeholder, f);
            ft.commit();
        } catch (Exception e) {
            String[] authors = new String[1];
            authors[0] = "-";
            BookInformationFragment f = BookInformationFragment.newInstance("Erro de conexão!", "-",authors, "-", "Erro de conexão com o banco de dados ou livro já inserido");
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.your_placeholder, f);
            ft.commit();
            Log.d("CONEXAO",e.getMessage());
        }

    }
}
