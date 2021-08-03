package ir.the_moment.carmichael_sms.ui.intro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ir.the_moment.carmichael_sms.ui.intro.ui.theme.CarmichaelTheme

class IntroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarmichaelTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MessageCard("hey")
                }
            }
        }
    }
}

@Composable
fun MessageCard(name: String) {
    Text(text = "hi $name!")
}

@Preview(showBackground = true)
@Composable
fun PreviewMessageCard() {
    MessageCard("Android")
}