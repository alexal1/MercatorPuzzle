package com.alex_aladdin.mercatorpuzzle.activities

import android.content.BroadcastReceiver
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R
import com.alex_aladdin.mercatorpuzzle.country.CountriesDisposition
import com.alex_aladdin.mercatorpuzzle.country.Country
import com.alex_aladdin.mercatorpuzzle.country.LatitudeBoundaries
import com.alex_aladdin.mercatorpuzzle.data.Continents
import com.alex_aladdin.mercatorpuzzle.fragments.ContinentDialogFragment
import com.alex_aladdin.mercatorpuzzle.fragments.FinishFragment
import com.alex_aladdin.mercatorpuzzle.fragments.LapFragment
import com.alex_aladdin.mercatorpuzzle.helpers.alpha
import com.alex_aladdin.mercatorpuzzle.helpers.createBitmapFrom
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.*
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.constants.MapboxConstants
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity() {

    companion object {

        const val TAG = "MercatorMapActivity"

        private val polygonsOnMap = HashMap<Country, ArrayList<Polygon>>()
        private val markersOnMap = HashMap<Marker, Continents>()

        /**
         * Remove country from the map.
         */
        fun removePolygons(country: Country) {
            polygonsOnMap[country]?.forEach { polygon -> polygon.remove() }
            polygonsOnMap[country]?.clear()
            polygonsOnMap.remove(country)
        }

    }

    private val compositeDisposable = CompositeDisposable()
    private var mapboxMap: MapboxMap? = null
    private var isMultiTouchAfterDrag = false
    private var newGameReceiver: BroadcastReceiver? = null
    private var continentChosenReceiver: BroadcastReceiver? = null
    private var progressReceiver: BroadcastReceiver? = null
    private var countriesLoadedReceiver: BroadcastReceiver? = null
    private var newLapReceiver: BroadcastReceiver? = null
    private var finishGameReceiver: BroadcastReceiver? = null

    var onLapFragmentReadyCallback = {
        supportFragmentManager.findFragmentByTag(LapFragment.TAG)?.let { lapFragment ->
            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                    .remove(lapFragment)
                    .commit()
            supportFragmentManager.popBackStack()
            MercatorApp.gameController.readyForNextLap()
        }
    }
    var onFinishFragmentReadyCallback = {
        MercatorApp.gameController.newGame()
    }
    var onMenuClickCallback = { _: View ->
        layoutDrawer.openDrawer(Gravity.START)
    }
    var onResultsClickCallback = { _: View ->
        val i = Intent(this@MapActivity, ResultsActivity::class.java)
        startActivity(i)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_map)

        mapView.onCreate(savedInstanceState)
        initMap {
            when {
                // Game is not initialized or not started
                MercatorApp.gameData?.isStarted() != true -> MercatorApp.gameController.newGame()
                // Game is finished
                MercatorApp.gameData?.isFinished() == true -> {
                    // Show FinishFragment if it is not added yet
                    if (supportFragmentManager.findFragmentByTag(FinishFragment.TAG) == null) {
                        supportFragmentManager
                                .beginTransaction()
                                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                                .add(R.id.layoutFragmentContainer, FinishFragment(), FinishFragment.TAG)
                                .addToBackStack(FinishFragment.TAG)
                                .commitAllowingStateLoss()
                    }
                }
                // Recreation
                else -> {
                    val shownCountriesCopy = MercatorApp.shownCountries.sortedBy { !it.isFixed }
                    MercatorApp.shownCountries.clear()
                    showCountries(shownCountriesCopy)
                }
            }
        }

        setListeners()
        setMenu()
        registerReceivers()
        setAppearance()
    }

    /**
     * All initial operations with the map.
     */
    private fun initMap(completion: () -> Unit) {
        mapView.getMapAsync { mapboxMap ->
            // Save object
            this.mapboxMap = mapboxMap
            mySurfaceView.mapboxMap = mapboxMap

            // Configure appearance
            mapboxMap.uiSettings.isRotateGesturesEnabled = false
            mapboxMap.uiSettings.isCompassEnabled = false
            mapboxMap.uiSettings.isAttributionEnabled = false
            mapboxMap.uiSettings.isLogoEnabled = false

            // Logo and attribution are created manually, so we need to set click listener
            val attributionView = mapView.findViewById<ImageView>(R.id.attributionView)
            imageMapboxAttribution.setOnClickListener {
                attributionView.callOnClick()
            }

            // Invoke callback
            completion()
        }

        mapView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mySurfaceView.clearCanvas()
                    mySurfaceView.isEnabled = false
                    myFloatingActionButton.isFocusedOnCountry = false
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mySurfaceView.isEnabled = true
                }
            }
            return@setOnTouchListener false
        }
    }

    private fun setListeners() {
        myFloatingActionButton.setOnClickListener {
            myFloatingActionButton.currentCountry?.let { focusCameraOn(it) }
            // Clear TopBarView's currentCountry if we've focused on another Country
            if (myFloatingActionButton.currentCountry != topBarView.currentCountry) {
                topBarView.currentCountry = null
            }
        }
    }

    private fun setMenu() {
        navigationView.itemIconTintList = null
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_new_game -> {
                    if (MercatorApp.gameData?.isStarted() == true) {
                        MercatorApp.gameController.saveGame()
                    }
                    MercatorApp.gameController.newGame()
                }

                R.id.nav_results -> {
                    val i = Intent(this@MapActivity, ResultsActivity::class.java)
                    startActivity(i)
                }

                R.id.nav_feedback -> {
                    val i = Intent(this@MapActivity, FeedbackActivity::class.java)
                    startActivity(i)
                }
            }
            layoutDrawer.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setAppearance() {
        mySurfaceView.setZOrderMediaOverlay(true)               // Show MySurfaceView above MapView
        mySurfaceView.holder.setFormat(PixelFormat.TRANSPARENT) // Make MySurfaceView transparent

        myFloatingActionButton.visibility = if (MercatorApp.shownCountries.isNotEmpty())
            View.VISIBLE
        else
            View.GONE
    }

    private fun registerReceivers() {
        newGameReceiver = MercatorApp.notificationsHelper.registerNewGameReceiver {
            // Remove all polygons
            polygonsOnMap.entries.map { it.key }.forEach { country -> removePolygons(country) }

            // Dispose all subscriptions
            compositeDisposable.clear()

            // Zoom
            val mapboxMapNotNull = mapboxMap ?: return@registerNewGameReceiver
            mapboxMapNotNull.setZoom(MapboxConstants.MINIMUM_ZOOM.toDouble())

            // Markers
            val iconFactory = IconFactory.getInstance(this@MapActivity)
            val bitmap = createBitmapFrom(R.drawable.point)
            val iconSolid = iconFactory.fromBitmap(bitmap)
            val iconTransparent = iconFactory.fromBitmap(bitmap.alpha(0.4f))
            for (continent in Continents.values()) {
                if (markersOnMap.values.contains(continent)) continue
                val marker = mapboxMapNotNull.addMarker(MarkerOptions()
                        .position(continent.center)
                        .icon(if (continent == Continents.EUROPE) iconSolid else iconTransparent))
                markersOnMap[marker] = continent
            }

            // Markers listeners
            mapboxMapNotNull.setOnMarkerClickListener { marker ->
                markersOnMap[marker]
                        ?.let { continent ->
                            if (continent == Continents.EUROPE) {
                                MercatorApp.gameController.chooseContinent(continent)
                            }
                            else {
                                val continentDialogFragment = ContinentDialogFragment.instance(continent)
                                continentDialogFragment.show(supportFragmentManager, ContinentDialogFragment.TAG)
                            }
                            true
                        }
                        ?: false
            }

            // Caption
            topBarView.showText(getString(R.string.top_bar_new_game))

            // Hide FAB
            myFloatingActionButton.visibility = View.GONE

            // Remove FinishFragment if it is added
            supportFragmentManager.findFragmentByTag(FinishFragment.TAG)?.let { finishFragment ->
                supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                        .remove(finishFragment)
                        .commit()
                supportFragmentManager.popBackStack()
            }
        }

        continentChosenReceiver = MercatorApp.notificationsHelper.registerContinentChosenReceiver { continent ->
            // Remove all markers
            markersOnMap.keys.forEach { marker ->
                mapboxMap?.removeMarker(marker)
            }
            markersOnMap.clear()

            // Caption
            val caption = getString(R.string.top_bar_loading) + getString(continent.stringId)
            topBarView.showText(caption)
        }

        progressReceiver = MercatorApp.notificationsHelper.registerProgressReceiver { progress ->
            progressBar.progress = progress
        }

        countriesLoadedReceiver = MercatorApp.notificationsHelper.registerCountriesLoadedReceiver {
            // Hide ProgressBar and TopBarView
            progressBar.progress = 0
            topBarView.hideText()

            // Randomize Countries' positions
            val rect = MercatorApp.gameData
                    ?.continent
                    ?.toCountry()
                    ?.getRect()
                    ?: return@registerCountriesLoadedReceiver
            val viewPort = ViewPort(
                    northeast = LatLng(rect.topLat, rect.rightLng),
                    southwest = LatLng(rect.bottomLat, rect.leftLng)
            )
            CountriesDisposition(viewPort).apply(MercatorApp.loadedCountries)

            // Start the first lap
            MercatorApp.gameController.readyForNextLap()
        }

        newLapReceiver = MercatorApp.notificationsHelper.registerNewLapReceiver { countries ->
            // Remove all polygons
            polygonsOnMap.entries.map { it.key }.forEach { country -> removePolygons(country) }

            // Dispose all subscriptions
            compositeDisposable.clear()

            // Focus camera on the continent and show countries
            val continent = MercatorApp.gameData?.continent ?: return@registerNewLapReceiver
            focusCameraOn(country = continent.toCountry(), withPadding = false) {
                showCountries(countries)
            }

            // Show FAB
            myFloatingActionButton.currentCountry = null
            val transitionFAB = Fade(Fade.IN)
            transitionFAB.addTarget(myFloatingActionButton)
            TransitionManager.beginDelayedTransition(layoutDrawer, transitionFAB)
            myFloatingActionButton.visibility = View.VISIBLE
        }

        finishGameReceiver = MercatorApp.notificationsHelper.registerFinishGameReceiver {
            // Show FinishFragment
            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                    .add(R.id.layoutFragmentContainer, FinishFragment(), FinishFragment.TAG)
                    .addToBackStack(FinishFragment.TAG)
                    .commitAllowingStateLoss()

            // Hide FAB
            myFloatingActionButton.visibility = View.GONE
        }
    }

    private fun unregisterReceivers() {
        newGameReceiver?.let { MercatorApp.notificationsHelper.unregisterReceiver(it) }
        continentChosenReceiver?.let { MercatorApp.notificationsHelper.unregisterReceiver(it) }
        progressReceiver?.let { MercatorApp.notificationsHelper.unregisterReceiver(it) }
        countriesLoadedReceiver?.let { MercatorApp.notificationsHelper.unregisterReceiver(it) }
        newLapReceiver?.let { MercatorApp.notificationsHelper.unregisterReceiver(it) }
        finishGameReceiver?.let { MercatorApp.notificationsHelper.unregisterReceiver(it) }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val isMultiTouchStarting = (event != null && event.actionMasked == MotionEvent.ACTION_POINTER_DOWN)
        val isMultiTouchGoing = (event != null && event.pointerCount > 1)
        val isMultiTouchFinishing = (event != null &&
                (event.actionMasked == MotionEvent.ACTION_POINTER_UP
                || event.action == MotionEvent.ACTION_UP
                || event.action == MotionEvent.ACTION_CANCEL))

        // Let mapView to handle multi touches that started immediately after country dragging
        if ((isMultiTouchGoing && isMultiTouchAfterDrag) || (isMultiTouchStarting && mySurfaceView.dragInProcess)) {
            if (mySurfaceView.dragInProcess) {
                val eventCopy = MotionEvent.obtain(event)
                eventCopy.action = MotionEvent.ACTION_CANCEL
                mySurfaceView.dispatchTouchEvent(eventCopy)
                mySurfaceView.clearCanvas()
                isMultiTouchAfterDrag = true
            }
            mapView.dispatchTouchEvent(event)
            return true
        }
        else {
            if (isMultiTouchFinishing) {
                isMultiTouchAfterDrag = false
            }
            return super.dispatchTouchEvent(event)
        }
    }

    override fun onBackPressed() {
        if (layoutDrawer.isDrawerOpen(Gravity.START)) {
            layoutDrawer.closeDrawer(Gravity.START)
            return@onBackPressed
        }

        (supportFragmentManager.findFragmentByTag(LapFragment.TAG) as? LapFragment)?.let { lapFragment ->
            lapFragment.onBackPressed()
            return@onBackPressed
        }

        (supportFragmentManager.findFragmentByTag(FinishFragment.TAG) as? FinishFragment)?.let { finishFragment ->
            finishFragment.onBackPressed()
            return@onBackPressed
        }

        super.onBackPressed()
    }

    /**
     * Draw country on the map.
     */
    fun drawCountry(country: Country) {
        if (mapboxMap == null) {
            Log.e(TAG, "Cannot draw country, mapboxMap is null!")
            return
        }
        else if (polygonsOnMap.containsKey(country)) {
            Log.e(TAG, "Cannot draw country, polygonsOnMap already contains one!")
            return
        }

        val countryPolygons = ArrayList<Polygon>()

        for (polygon in country.vertices) {
            // Condition from GeoJSON specification
            if (polygon.size < 4) {
                Log.e(TAG, "Incorrect polygon in ${country.name}!")
                continue
            }

            val polygonOptions = PolygonOptions()
                    .fillColor(country.color)
                    .addAll(polygon)
            val newPolygon = mapboxMap!!.addPolygon(polygonOptions)
            countryPolygons.add(newPolygon)
        }

        polygonsOnMap[country] = countryPolygons
    }

    /**
     * Redraw given countries in an order that fixed countries are on bottom.
     */
    fun redrawCountries(countries: List<Country>) {
        countries.forEach { removePolygons(it) }
        val (fixed, notFixed) = countries.partition { it.isFixed }
        fixed.forEach { drawCountry(it) }
        notFixed.forEach { drawCountry(it) }
    }

    /**
     * Move camera close to the given country.
     */
    private fun focusCameraOn(country: Country, withPadding: Boolean = true, completion: (() -> Unit)? = null) {
        val rect = country.getRect()
        val latLngBounds = LatLngBounds.Builder()
                .include(LatLng(rect.topLat, rect.rightLng))    // northeast
                .include(LatLng(rect.bottomLat, rect.leftLng))  // southwest
                .build()

        val padding = if (withPadding)
            (maxOf(MercatorApp.screen.x, MercatorApp.screen.y) / 8).toInt()
        else
            0

        mapboxMap?.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, padding), 1000)
        mySurfaceView.countriesAnimator?.cancel()
        mySurfaceView.clearCanvas()

        // Add animation finish listener
        mapboxMap?.addOnCameraIdleListener(object : MapboxMap.OnCameraIdleListener {

            override fun onCameraIdle() {
                mapboxMap?.removeOnCameraIdleListener(this)
                completion?.invoke()
            }

        })
    }

    private fun showCountries(countries: List<Country>) {
        for (country in countries) {
            country.color = if (country.isFixed) MercatorApp.countryFixedColor else MercatorApp.obtainColor()
            MercatorApp.shownCountries.add(country)
            compositeDisposable.addAll(
                    myFloatingActionButton.subscribeOn(country.currentCenterObservable),
                    topBarView.subscribeOn(country.currentCenterObservable),
                    this@MapActivity.subscribeOn(country.currentCenterObservable)
            )
        }
        myFloatingActionButton.currentCountry = MercatorApp.shownCountries.firstOrNull()
        mySurfaceView.showCountries(MercatorApp.shownCountries)
    }

    private fun subscribeOn(observable: Observable<Country>): Disposable {
        return observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            // Show LapFragment if lap is finished and LapFragment is not added yet
                            if (MercatorApp.shownCountries.all { it.isFixed }
                                    && supportFragmentManager.findFragmentByTag(LapFragment.TAG) == null) {

                                supportFragmentManager
                                        .beginTransaction()
                                        .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                                        .add(R.id.layoutFragmentContainer, LapFragment(), LapFragment.TAG)
                                        .addToBackStack(LapFragment.TAG)
                                        .commitAllowingStateLoss()
                                topBarView.hideText()

                                // Hide FAB
                                myFloatingActionButton.visibility = View.GONE
                            }
                        }
                )
    }

    private fun setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Make status bar transparent
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = ContextCompat.getColor(this@MapActivity, R.color.grey80)

            // Set top margin for TopBarView and ProgressBar
            var statusBarHeight = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                statusBarHeight = resources.getDimensionPixelSize(resourceId)
            }
            topBarView.topMargin = statusBarHeight
            (progressBar.layoutParams as ViewGroup.MarginLayoutParams).topMargin = statusBarHeight
            FinishFragment.topMargin = statusBarHeight
        }
    }

    public override fun onStart() {
        super.onStart()
        mapView.onStart()
        setStatusBar()
    }

    public override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        unregisterReceivers()

        // Clear static objects
        Companion.polygonsOnMap.clear()
        Companion.markersOnMap.clear()

        // Dispose all subscriptions
        compositeDisposable.clear()
    }

    /**
     * Representation of a certain area on the map.
     */
    data class ViewPort(val northeast: LatLng, val southwest: LatLng) {

        constructor() : this(
                northeast = LatLng(LatitudeBoundaries.MAX_MAP_LATITUDE, 180.0),
                southwest = LatLng(-LatitudeBoundaries.MAX_MAP_LATITUDE, -180.0)
        )

    }

}