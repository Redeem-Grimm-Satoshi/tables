package org.opendatakit.espresso;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.web.webdriver.DriverAtoms;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.tables.activities.MainActivity;
import org.opendatakit.util.DisableAnimationsRule;
import org.opendatakit.util.UAUtils;

import static android.support.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.getText;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.containsString;

/**
 * This test can only be used with the index from the large
 * data set app and is used for very specific purposes.
 *
 * This should never be run on the build server!
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class WebViewPerfTest {
   private Boolean initSuccess = null;
   private UiDevice mDevice;

   // Run through the app for performance timings
   private static final int numOfTimesToRun = 100;
   private static final int numOfMsToSleep = 10;
   private static final int maxRetriesForIter = 25;

   private static final String LIMIT_TO_USE = "200";
   private static final String OFFSET_TO_USE = "0";
   private static final String OS_TO_USE = "5.1";
   private static final String DEVICE_TO_USE = "Nexus 6";

   // DB_TO_USE Options are android and custom
   enum TEST_DB_TYPE {
      ANDROID,
      CUSTOM
   }
   private static final TEST_DB_TYPE DB_TO_USE = TEST_DB_TYPE.ANDROID;

   enum TEST_TRUE_FALSE {
      TRUE,
      FALSE
   }
   // SERVICES_USED Options are true and false
   private static final TEST_TRUE_FALSE SERVICES_USED = TEST_TRUE_FALSE.TRUE;

   // ALL_IN_ONE_APK_USED Options are true and false
   private static final TEST_TRUE_FALSE ALL_IN_ONE_APK_USED = TEST_TRUE_FALSE.FALSE;


   @ClassRule
   public static DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

   @Rule
   public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<MainActivity>(
       MainActivity.class, false, true) {
      @Override
      protected void beforeActivityLaunched() {
         super.beforeActivityLaunched();

         if (initSuccess == null) {
            mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            initSuccess = UAUtils.turnOnCustomHome(mDevice);
         }
      }

      @Override
      protected void afterActivityLaunched() {
         super.afterActivityLaunched();

         onWebView().forceJavascriptEnabled();
      }
   };

   @Before
   public void setup() {
      UAUtils.assertInitSucess(initSuccess);
   }

   @Test public void performanceTestForLargeDataSet() {
      //if (true) return;

      // Set the limit
      onWebView()
              .withElement(findElement(Locator.ID, "limit"))
              .perform(DriverAtoms.webKeys(LIMIT_TO_USE));

      // Set the offset
      onWebView()
              .withElement(findElement(Locator.ID, "offset"))
              .perform(DriverAtoms.webKeys(OFFSET_TO_USE));

      // Set the os version
      onWebView()
              .withElement(findElement(Locator.ID, "os"))
              .perform(DriverAtoms.webKeys(OS_TO_USE));

      // Set the device
      onWebView()
              .withElement(findElement(Locator.ID, "device"))
              .perform(DriverAtoms.webKeys(DEVICE_TO_USE));

      // Set the database
      switch (DB_TO_USE) {
         case ANDROID:
            onWebView()
                    .withElement(findElement(Locator.ID, "db-android"))
                    .perform(webClick());
            break;
         case CUSTOM:
            onWebView()
                    .withElement(findElement(Locator.ID, "db-custom"))
                    .perform(webClick());
            break;
      }

      // Set if services is used
      switch (SERVICES_USED) {
         case TRUE:
            onWebView()
                    .withElement(findElement(Locator.ID, "services-true"))
                    .perform(webClick());
            break;
         case FALSE:
            onWebView()
                    .withElement(findElement(Locator.ID, "services-false"))
                    .perform(webClick());
            break;
      }

      // Set if all-in-one-apk is used
      switch (ALL_IN_ONE_APK_USED) {
         case TRUE:
            onWebView()
                    .withElement(findElement(Locator.ID, "all-in-one-true"))
                    .perform(webClick());
            break;
         case FALSE:
            onWebView()
                    .withElement(findElement(Locator.ID, "all-in-one-false"))
                    .perform(webClick());
            break;
      }

      // Click the submit button
      onWebView()
              .withElement(findElement(Locator.ID, "submit"))
              .perform(webClick());

      int numOfTimesNextButtonHit = 0;
      for (int i = 0; i < numOfTimesToRun; i++)
      {
         boolean nextButtonCntMatches = false;
         int iterRead = 0;
         while (!nextButtonCntMatches) {
            nextButtonCntMatches = true;
            try {
               String nextButtonIterStr = Integer.toString(numOfTimesNextButtonHit);

               onWebView().withElement(findElement(Locator.ID, "iter"))
                       .check(webMatches(getText(), containsString(nextButtonIterStr)));

               onWebView()
                   // Find the input element by ID
                   .withElement(findElement(Locator.ID, "nextButton"))
                       // Launch into teahouses
                   .perform(webClick());
               // Increment the number of times the next button has been hit
               // We do not continue unless the test and app are in agreement
               numOfTimesNextButtonHit++;
               Thread.sleep(numOfMsToSleep);
            } catch (RuntimeException e) {
               //e.printStackTrace();
               System.out.println("Failed to find the iter element");
               nextButtonCntMatches = false;
            } catch (InterruptedException ie) {
               System.out.println("Error with thread sleep");
            } catch (junit.framework.AssertionFailedError afe) {
               nextButtonCntMatches = false;
               if (iterRead > maxRetriesForIter) {
                  System.out.println("Max retry for next button hits exceeded: " + maxRetriesForIter);
                  fail();
               } else {
                  System.out.println("Next button hits != web view hits after retry:" +  iterRead);
                  iterRead++;
               }

            }
         }
         System.out.println("Number of iterations = " + i);
      }
   }
}