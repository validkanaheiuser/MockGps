package moe.yukoDotMoe.cuteanimegirl.ui.components

import android.location.Address
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.sharp.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.yukoDotMoe.cuteanimegirl.R
import moe.yukoDotMoe.cuteanimegirl.models.LatLng
import moe.yukoDotMoe.cuteanimegirl.extensions.displayString
import moe.yukoDotMoe.cuteanimegirl.extensions.prettyPrint
import moe.yukoDotMoe.cuteanimegirl.service.VibratorService
import moe.yukoDotMoe.cuteanimegirl.ui.theme.ButtonGreen
import moe.yukoDotMoe.cuteanimegirl.ui.theme.ButtonRed
import moe.yukoDotMoe.cuteanimegirl.ui.theme.Gold

@Composable
fun FooterComponent(
    modifier: Modifier = Modifier,
    address: Address?,
    latLng: LatLng,
    isMocking: Boolean,
    isFavorite: Boolean = false,
    onStart: () -> Unit,
    onFavorite: () -> Unit,
    onIpLocation: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // Location section
            Row {
                Icon(
                    modifier = Modifier
                        .height(32.dp)
                        .width(32.dp)
                        .align(Alignment.CenterVertically),
                    imageVector = Icons.Filled.Home,
                    tint = Color.Gray,
                    contentDescription = stringResource(R.string.address)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth(0.85f),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    text = address?.displayString() ?: "\n"
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Coordinates section
            Row {
                Icon(
                    modifier = Modifier
                        .height(32.dp)
                        .width(32.dp)
                        .align(Alignment.CenterVertically),
                    imageVector = Icons.Filled.Place,
                    tint = Color.Red,
                    contentDescription = stringResource(R.string.coordinates)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    color = MaterialTheme.colorScheme.onSurface,
                    text = latLng.prettyPrint()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // IP Location button
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                onClick = { onIpLocation() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = stringResource(R.string.use_ip_location),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.use_ip_location),
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Button row
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                // Favorite button.
                IconButton(
                    modifier = Modifier
                        .width(32.dp)
                        .height(32.dp)
                        .align(Alignment.CenterVertically),
                    onClick = {
                        onFavorite()
                        VibratorService.vibrate()
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (isFavorite) Gold else Color.LightGray,
                    )
                ) {
                    Icon(
                        modifier = Modifier
                            .height(32.dp)
                            .width(32.dp)
                            .align(Alignment.CenterVertically),
                        imageVector = Icons.Sharp.Star,
                        contentDescription = stringResource(R.string.toggle_favorite)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Toggle mocking button
                IconToggleButton(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth(),
                    checked = isMocking,
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        checkedContainerColor = ButtonRed,
                        containerColor = ButtonGreen,
                        contentColor = Color.White
                    ),
                    onCheckedChange = {
                        onStart()
                    },
                ) {
                    Row {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.CenterVertically),
                            imageVector = if (isMocking) Icons.Filled.Close else Icons.Filled.PlayArrow,
                            tint = Color.White,
                            contentDescription = if (isMocking) stringResource(R.string.stop_mocking) else stringResource(R.string.start_mocking)
                        )

                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterVertically),
                            style = TextStyle(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            text = if (isMocking) stringResource(R.string.stop_mocking) else stringResource(R.string.start_mocking)
                        )
                    }
                }
            }

        }
    }
}
