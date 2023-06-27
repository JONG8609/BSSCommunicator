package com.otosone.bssmgr.navFragments;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.otosone.bssmgr.R;
import com.otosone.bssmgr.databinding.FragmentSocketSpecBinding;
import com.otosone.bssmgr.databinding.FragmentStationBinding;


public class SocketSpecFragment extends Fragment {

    FragmentSocketSpecBinding binding;
    
    
    public SocketSpecFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_socket_spec, container, false);
        View root = binding.getRoot();
        if(getActivity()!=null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        Databind();
        
        return root;
    }

    private void Databind() {
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}