package ru.breffi.smartlibrary.host

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import dagger.android.AndroidInjection
import ru.breffi.clm.ui.library.host.Navigation
import ru.breffi.smartlibrary.PresentationCache
import ru.breffi.smartlibrary.R
import ru.breffi.smartlibrary.content.ContentFragment
import ru.breffi.smartlibrary.main.MainFragment
import ru.breffi.smartlibrary.media.MediaFilesFragment
import ru.breffi.smartlibrary.slides.SlidesTreeFragment
import ru.breffi.story.domain.models.PresentationEntity
import java.util.*




class HostActivity : AppCompatActivity(), Navigation {

    val screenStack: Stack<Fragment> = Stack()

    var presentationToLaunch: PresentationEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        //Fixing https://stackoverflow.com/questions/19545889/app-restarts-rather-than-resumes/23220151
        if (!isTaskRoot()
            && intent?.hasCategory(Intent.CATEGORY_LAUNCHER) ?: false
            && intent.getAction()?.equals(Intent.ACTION_MAIN) ?: false
        ) {
            finish()
            return
        }
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)

        showMain()
    }

    override fun showMain() {
        setFragment(MainFragment())

        presentationToLaunch?.let {
            showContent(it)
            presentationToLaunch = null
        }
    }

    override fun showContent(presentationEntity: PresentationEntity) {
        PresentationCache.put(presentationEntity)
        addFragment(ContentFragment.newInstance(presentationEntity.id), ContentFragment.TAG)
    }

    override fun showMedia(presentationId: Int) {
        addFragment(MediaFilesFragment.newInstance(presentationId))
    }

    override fun showSlides(presentationId: Int) {
        addFragment(SlidesTreeFragment.newInstance(presentationId), SlidesTreeFragment.TAG)
    }

    override fun back() {
        val currentFragment = getCurrentFragment()
        val consumed = currentFragment is BackConsumer && currentFragment.onBackPressed()
        if (!consumed) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun setFragment(fragment: Fragment, tag: String? = null) {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        if (screenStack.isNotEmpty()) {
            screenStack.clear()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.contentLayout, fragment, tag)
            .commit()
        screenStack.push(fragment)
    }

    private fun addFragment(fragment: Fragment, tag: String? = null) {
        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .add(R.id.contentLayout, fragment, tag)
            .addToBackStack(tag)
            .commit()
        screenStack.push(fragment)
    }

    private fun getCurrentFragment() : Fragment? {
        return if (screenStack.isEmpty()) null else screenStack.peek()
    }

    override fun onBackPressed() {
        back()
    }
}
