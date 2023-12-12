package com.example.moneyminder

import loadModelFile
import org.junit.Assert.assertEquals
import org.junit.Test
import preprocessInput

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun testLoadModelFile() {
        // Đường dẫn đến file model tflite
        val modelPath = "path/to/your/model.tflite"
        val modelBuffer = loadModelFile(modelPath)

        // Kiểm tra xem buffer có được tạo thành công không
        assertEquals(true, modelBuffer.isDirect)
        assertEquals(modelBuffer.capacity(), modelBuffer.remaining())
    }

    @Test
    fun testPreprocessInput() {
        val transactionName = "Mua sắm"
        val transactionDescription = "Mua đồ cho gia đình"
        val inputArray = preprocessInput(transactionName, transactionDescription)

        // Kiểm tra xem mảng đầu ra có được tạo thành công không
        assertEquals(transactionName.length + transactionDescription.length, inputArray.size)

        // Kiểm tra giá trị đầu tiên của mảng có phải là giá trị số hóa của ký tự đầu tiên trong tên giao dịch không
        assertEquals(transactionName[0].toFloat(), inputArray[0])
    }
}