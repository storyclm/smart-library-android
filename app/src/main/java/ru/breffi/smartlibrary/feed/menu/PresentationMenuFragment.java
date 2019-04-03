package ru.breffi.smartlibrary.feed.menu;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import ru.breffi.smartlibrary.R;

import java.io.Serializable;


public class PresentationMenuFragment extends BottomSheetDialogFragment {

    public static final String TAG = "PresentationMenuFragment";
    public static final String PRESENTATION_ID = "PRESENTATION_ID";
    public static final String ITEM_CLICK = "ITEM_CLICK";
    TextView shareView;
    TextView updateView;
    TextView deleteTextView;
    private PresentationMenuItemClickListener presentationMenuItemClickListener;
    private int presentationId = -1;

    public static PresentationMenuFragment newInstance(int presentationId, PresentationMenuItemClickListener presentationMenuItemClickListener) {
        Bundle args = new Bundle();
        PresentationMenuFragment fragment = new PresentationMenuFragment();
        args.putInt(PRESENTATION_ID, presentationId);
        args.putSerializable(ITEM_CLICK, presentationMenuItemClickListener);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_presentation_menu, container, false);
        shareView = view.findViewById(R.id.share_text_view);
//        updateView = view.findViewById(R.id.update_text_view);
        deleteTextView = view.findViewById(R.id.delete_text_view);
        if (getArguments() != null) {
            presentationId = getArguments().getInt(PRESENTATION_ID);
            presentationMenuItemClickListener = (PresentationMenuItemClickListener) getArguments().getSerializable(ITEM_CLICK);
        }
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showWrapContentHeight(view);
        initListeners();
    }

    private void showWrapContentHeight(View view) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            FrameLayout bottomSheet = dialog.findViewById(android.support.design.R.id.design_bottom_sheet);
            BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setPeekHeight(0);
        });
    }

    private void initListeners() {
        shareView.setOnClickListener(v -> {
            presentationMenuItemClickListener.onMenuItemClick(MessageMenuItem.SHARE, presentationId);
            dismiss();
        });
//        updateView.setOnClickListener(v -> {
//            presentationMenuItemClickListener.onMenuItemClick(MessageMenuItem.UPDATE, presentationId);
//            dismiss();
//        });
        deleteTextView.setOnClickListener(v -> {
            presentationMenuItemClickListener.onMenuItemClick(MessageMenuItem.DELETE, presentationId);
            dismiss();
        });
    }

    public PresentationMenuItemClickListener getPresentationMenuItemClickListener() {
        return presentationMenuItemClickListener;
    }

    public void setMessageMenuItemClickListener(PresentationMenuItemClickListener presentationMenuItemClickListener) {
        this.presentationMenuItemClickListener = presentationMenuItemClickListener;
    }

    public enum MessageMenuItem {
        UPDATE, DELETE, SHARE
    }

    public interface PresentationMenuItemClickListener extends Serializable {
        void onMenuItemClick(MessageMenuItem item, int presentationId);
    }
}
