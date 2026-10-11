package com.spendwise

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.spendwise.util.ExportHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `test launch MainActivity`() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        assertNotNull(controller.get())
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SpendWise", appName)
    }

    @Test
    fun `test json backup parsing with android json`() {
        val sampleJson = """
            {
              "categories": [
                {"id": 1, "name": "Food & Dining", "iconName": "restaurant", "colorHex": "#F59E0B", "budget": 10000.0, "isDefault": true}
              ],
              "expenses": [
                {"id": 1, "name": "Dinner", "amount": 1500.0, "dateMillis": 1740000000000, "categoryId": 1, "note": "Pizza", "recurring": false, "createdAt": 1740000000000}
              ]
            }
        """.trimIndent()

        val parsed = ExportHelper.parseBackupJson(sampleJson)
        assertNotNull(parsed)
        assertEquals(1, parsed!!.first.size)
        assertEquals("Food & Dining", parsed.first[0].name)
        assertEquals(1, parsed.second.size)
        assertEquals("Dinner", parsed.second[0].name)
        assertEquals(1500.0, parsed.second[0].amount, 0.001)
    }
}
