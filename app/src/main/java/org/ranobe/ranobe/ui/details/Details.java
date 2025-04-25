package org.ranobe.ranobe.ui.details;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import org.ranobe.ranobe.R;
import org.ranobe.ranobe.config.Ranobe;
import org.ranobe.ranobe.config.RanobeSettings;
import org.ranobe.ranobe.database.RanobeDatabase;
import org.ranobe.ranobe.databinding.FragmentDetailsBinding;
import org.ranobe.ranobe.models.Novel;
import org.ranobe.ranobe.ui.chapters.Chapters;
import org.ranobe.ranobe.ui.details.viewmodel.DetailsViewModel;
import org.ranobe.ranobe.ui.error.Error;

import java.util.List;
import java.util.Locale;

public class Details extends Fragment {
    private FragmentDetailsBinding binding;
    private DetailsViewModel viewModel;

    private Novel novel;

    public Details() {
        // Required
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            novel = getArguments().getParcelable(Ranobe.KEY_NOVEL);
            RanobeSettings.get().setCurrentSource(novel.sourceId).save();
        }
        viewModel = new ViewModelProvider(requireActivity()).get(DetailsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        setUpListeners();
        setUpObservers();
        return binding.getRoot();
    }

    private void setUpListeners() {
        binding.readChapter.setOnClickListener(v -> navigateToChapterList());
        binding.addToLib.setOnClickListener(v -> addToLibrary(novel));
        binding.share.setOnClickListener(v -> shareLink());
        binding.progress.show();
    }

    private void shareLink() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TITLE, String.format(Locale.getDefault(), "Read %s light novel for free on Ranobe %s or visit %s", novel.name, Ranobe.RANOBE_GITHUB_LINK, novel.url));
        sendIntent.putExtra(Intent.EXTRA_TEXT, novel.url);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private void setUpObservers() {
        viewModel.getError().observe(getViewLifecycleOwner(), this::setUpError);
        viewModel.details(novel).observe(getViewLifecycleOwner(), this::setUpUi);
        RanobeDatabase.database().readingList().countOfReadForNovel(novel.url).observe(getViewLifecycleOwner(), count -> {
            if (count > 0) binding.readChapter.setText(R.string.continue_btn);
        });
    }

    private void setUpError(String error) {
        binding.progress.hide();
        Error.navigateToErrorFragment(requireActivity(), error);
    }

    private void navigateToChapterList() {
        if (novel == null) return;
        Bundle bundle = new Bundle();
        bundle.putParcelable(Ranobe.KEY_NOVEL, novel);
        Chapters chapters = new Chapters();
        chapters.setArguments(bundle);
        chapters.show(getParentFragmentManager(), "chapters-sheet");
    }

    private void setUpUi(Novel novel) {
        this.novel = novel;
        Glide.with(binding.novelCover.getContext()).load(novel.cover).into(binding.novelCover);
        Glide.with(binding.novelCover.getContext()).load(novel.cover).into(binding.novelCoverHeader);
        binding.novelName.setText(novel.name);
        binding.rating.setRating(novel.rating);
        binding.summary.setText(novel.summary);
        binding.status.setText(novel.status);
        addChips(novel.genres);

        if (novel.authors != null) {
            binding.authors.setText(String.join(", ", novel.authors));
        }

        binding.progress.hide();
    }

    private void addChips(List<String> genres) {
        if (genres == null) return;

        binding.genresLayout.removeAllViews();
        for (String genre : genres) {
            if (genre.trim().isEmpty()) continue;
            Chip chip = new Chip(binding.genresLayout.getContext());
            chip.setText(genre);
            binding.genresLayout.addView(chip);
        }
    }

    private void addToLibrary(Novel novel) {
        if (novel == null) return;
        RanobeDatabase.databaseExecutor.execute(() -> RanobeDatabase.database().novels().save(novel));
        Snackbar.make(binding.getRoot(), "Added novel to library", Snackbar.LENGTH_SHORT).show();
    }
}
