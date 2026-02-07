package com.example.elizarchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.elizarchat.ui.screens.ElizarNavigation
import com.example.elizarchat.ui.theme.ElizarChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ElizarChatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ElizarNavigation()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    ElizarChatTheme {
        ElizarNavigation()
    }
}

@Composable
fun getElizarChatApplication(): ElizarChatApplication {
    val context = LocalContext.current
    return context.applicationContext as ElizarChatApplication
}

// test12345@test11.com
// edcrfv