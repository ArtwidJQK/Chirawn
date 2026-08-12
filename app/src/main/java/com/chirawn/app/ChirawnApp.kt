package com.chirawn.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chirawn.app.data.*
import com.chirawn.app.game.*
import kotlinx.coroutines.launch
import java.util.Calendar

private enum class Route { HOME, GAMES, PROFILE, SUDOKU, TWO048, SLIDING }

@Composable
fun ChirawnApp(repo: HubRepository) {
    var route by rememberSaveable { mutableStateOf(Route.HOME) }
    val stack = remember { mutableStateListOf<Route>() }

    fun go(r: Route) {
        stack.add(route)
        route = r
    }

    BackHandler(enabled = stack.isNotEmpty()) {
        route = stack.removeAt(stack.size - 1)
    }

    AnimatedContent(route, label = "route") { r ->
        when (r) {
            Route.HOME -> Home(repo, { go(Route.GAMES) }, { go(Route.PROFILE) })
            Route.GAMES -> GameCenter(
                repo,
                { go(Route.SUDOKU) },
                { go(Route.TWO048) },
                { go(Route.SLIDING) }
            )
            Route.PROFILE -> Profile(repo)
            Route.SUDOKU -> SudokuScreen(repo)
            Route.TWO048 -> Two048Screen(repo)
            Route.SLIDING -> SlidingScreen(repo)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Shell(title: String, content: @Composable ColumnScope.() -> Unit) = Scaffold(
    topBar = {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    title,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
) { p ->
    Column(
        Modifier
            .padding(p)
            .padding(horizontal = 24.dp)
            .fillMaxSize(),
        content = content
    )
}

@Composable
private fun AvatarImage(resId: Int, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val painter = if (resId != 0) painterResource(resId) else null
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.Person,
                null,
                Modifier.size(size * 0.6f),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun Home(repo: HubRepository, games: () -> Unit, profile: () -> Unit) {
    val p by repo.profile.collectAsStateWithLifecycle(null)
    val total by repo.total.collectAsStateWithLifecycle(0)
    val recent by repo.recent.collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    val streakEngine = remember { StreakEngine() }

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Chào buổi sáng"
        in 12..17 -> "Chào buổi chiều"
        else -> "Chào buổi tối"
    }

    Shell("Chirawn") {
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(greeting, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    p?.nickname ?: "Bạn",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            AvatarImage(
                resId = p?.avatarRes ?: 0,
                size = 56.dp,
                modifier = Modifier.clickable(onClick = profile)
            )
        }
        
        // Artwid Supporter Area
        Spacer(Modifier.height(16.dp))
        val brokenDate = p?.streakBrokenDate
        val canRecover = remember(brokenDate) { streakEngine.canRecover(brokenDate) }
        
        Surface(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, "Artwid", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Artwid", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(4.dp))
                if (canRecover && p != null && p!!.previousStreak > 0) {
                    Text("Ơ kìa, chuỗi ${p!!.previousStreak} ngày của mình bị đứt rồi. Bạn có muốn Artwid khôi phục lại không?")
                    TextButton(onClick = { scope.launch { repo.restoreStreak() } }) {
                        Text("Khôi phục ngay")
                    }
                } else {
                    val currentStr = p?.currentStreak ?: 0
                    val artwidMsg = when {
                        currentStr >= 7 -> "Tuyệt vời! Bạn đang có chuỗi $currentStr ngày rồi đó. Cứ tiếp tục nhé!"
                        currentStr >= 1 -> "Chào mừng bạn quay lại. Hãy hoàn thành một ván game để duy trì chuỗi nhé."
                        else -> "Hôm nay là một ngày đẹp trời để bắt đầu điều gì đó mới mẻ."
                    }
                    Text(artwidMsg, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        ElevatedCard(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = games),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                Modifier.padding(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.SportsEsports, null, Modifier.size(42.dp))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Game Center", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Ba trò chơi, một không gian riêng tư")
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Stat("Đã chơi", total.toString(), Modifier.weight(1f))
            Stat("Chuỗi hiện tại", (p?.currentStreak ?: 0).toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(22.dp))
        Text("Hoạt động gần đây", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (recent.isEmpty()) {
            Text(
                "Chưa có ván nào. Hãy bắt đầu một trò chơi.",
                Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn {
                items(recent) { s ->
                    Text(
                        "${s.gameType.name.replace('_', ' ')} · ${if (s.completed) "hoàn thành" else "đã dừng"}",
                        Modifier.padding(vertical = 9.dp)
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        OutlinedButton(
            onClick = profile,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
        ) {
            Icon(Icons.Default.Person, null)
            Spacer(Modifier.width(8.dp))
            Text("Hồ sơ của bạn")
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    Surface(
        modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
private fun GameCenter(repo: HubRepository, sudoku: () -> Unit, two: () -> Unit, sliding: () -> Unit) {
    val bests by repo.bests.collectAsStateWithLifecycle(emptyList())
    fun b(t: GameType) = bests.firstOrNull { it.gameType == t }
    Shell("Game Center") {
        Spacer(Modifier.height(12.dp))
        Text("Chọn một khoảng nghỉ nhỏ", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(18.dp))
        GameCard(
            "Sudoku",
            "Lắng lại với những con số.",
            b(GameType.SUDOKU)?.bestTimeSeconds?.let { "Tốt nhất ${it}s" },
            sudoku
        )
        GameCard(
            "2048",
            "Ghép ô, giữ nhịp và đi xa hơn.",
            b(GameType.GAME_2048)?.bestScore?.takeIf { it > 0 }?.let { "Điểm cao $it" },
            two
        )
        GameCard(
            "Sliding Puzzle",
            "Sắp xếp từng bước thật gọn gàng.",
            b(GameType.SLIDING)?.bestTimeSeconds?.let { "Tốt nhất ${it}s" },
            sliding
        )
    }
}

@Composable
private fun GameCard(name: String, desc: String, best: String?, play: () -> Unit) {
    ElevatedCard(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {
        Column(Modifier.padding(19.dp)) {
            Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(desc, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (best != null) {
                Text(best, Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.primary)
            }
            Button(
                onClick = play,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 10.dp)
            ) {
                Text("Chơi")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Profile(repo: HubRepository) {
    val p by repo.profile.collectAsStateWithLifecycle(null)
    val total by repo.total.collectAsStateWithLifecycle(0)
    val scope = rememberCoroutineScope()
    var name by remember(p) { mutableStateOf(p?.nickname ?: "Bạn") }

    val avatars = listOf(
        R.drawable.avt1,
        R.drawable.avt2,
        R.drawable.avt3
    )

    Shell("Hồ sơ") {
        Spacer(Modifier.height(20.dp))
        
        // Avatar Selection
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            avatars.forEach { resId ->
                val isSelected = p?.avatarRes == resId
                Box(
                    Modifier
                        .padding(8.dp)
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { scope.launch { repo.updateAvatar(resId) } }
                ) {
                    AvatarImage(resId, 72.dp)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Biệt danh") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Button(
            onClick = { scope.launch { repo.nickname(name) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Lưu biệt danh")
        }
        
        Spacer(Modifier.height(32.dp))
        Text("Thống kê của bạn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Stat("Tổng số ván", total.toString(), Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Text("Tổng thời gian chơi —", style = MaterialTheme.typography.bodyLarge)
        Text("Thành tựu hay nhất —", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun GameHeader(score: String, time: Long, action: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            score,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text("${time / 60}:${(time % 60).toString().padStart(2, '0')}")
        IconButton(onClick = action) {
            Icon(Icons.Default.Refresh, null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SudokuScreen(repo: HubRepository) {
    val vm = remember { SudokuViewModel(repo) }
    Shell("Sudoku") {
        GameHeader("Lỗi: ${vm.mistakes}", vm.seconds) { vm.newGame() }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Easy", "Medium", "Hard").forEach { d ->
                FilterChip(
                    selected = d == vm.difficulty,
                    onClick = { vm.newGame(d) },
                    label = { Text(d) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            (0..8).forEach { r ->
                Row(Modifier.weight(1f)) {
                    (0..8).forEach { c ->
                        val i = r * 9 + c
                        val sel = vm.selected == i
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(1.dp)
                                .background(if (sel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { vm.select(i) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (vm.board[i] == 0) "" else vm.board[i].toString(),
                                fontWeight = if (vm.fixed[i]) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            (1..9).forEach { n ->
                FilledTonalButton(
                    onClick = { vm.input(n) },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(35.dp)
                ) {
                    Text("$n")
                }
            }
        }
        TextButton(
            onClick = { vm.input(0) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Xóa ô")
        }
        if (vm.finished) {
            Text(
                text = "Hoàn thành!",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun Two048Screen(repo: HubRepository) {
    val vm = remember { Game2048ViewModel(repo) }
    Shell("2048") {
        GameHeader("Điểm: ${vm.score}", vm.seconds) { vm.restart() }
        Text(
            if (vm.over) "Kết thúc — chạm làm mới để chơi lại" else if (vm.won) "Bạn đã đạt 2048!" else "Vuốt để di chuyển",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))
        var drag by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
        Column(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFB7AFA6))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { drag = androidx.compose.ui.geometry.Offset.Zero },
                        onDragEnd = {
                            if (kotlin.math.abs(drag.x) > kotlin.math.abs(drag.y))
                                vm.move(if (drag.x > 0) 3 else 2)
                            else if (drag.y != 0f)
                                vm.move(if (drag.y > 0) 1 else 0)
                        }
                    ) { _, amount ->
                        drag += amount
                    }
                }
        ) {
            (0..3).forEach { r ->
                Row(Modifier.weight(1f)) {
                    (0..3).forEach { c ->
                        val v = vm.cells[r * 4 + c]
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(tileColor(v)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (v == 0) "" else "$v",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (v >= 8) Color.White else Color(0xFF403A35)
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { vm.move(0) }) { Text("↑") }
            Button(onClick = { vm.move(2) }) { Text("←") }
            Button(onClick = { vm.move(1) }) { Text("↓") }
            Button(onClick = { vm.move(3) }) { Text("→") }
        }
    }
}

private fun tileColor(v: Int) = when (v) {
    0 -> Color(0xFFD6D0C8)
    2 -> Color(0xFFEEE4DA)
    4 -> Color(0xFFEDE0C8)
    8 -> Color(0xFFF2B179)
    16 -> Color(0xFFF59563)
    32 -> Color(0xFFF67C5F)
    64 -> Color(0xFFF65E3B)
    else -> Color(0xFF8F7A66)
}

@Composable
private fun SlidingScreen(repo: HubRepository) {
    val vm = remember { SlidingViewModel(repo) }
    Shell("Sliding Puzzle") {
        GameHeader("Nước đi: ${vm.moves}", vm.seconds) { vm.restart() }
        Text("Sắp xếp từ 1 đến 8", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            (0..2).forEach { r ->
                Row(Modifier.weight(1f)) {
                    (0..2).forEach { c ->
                        val i = r * 3 + c
                        val v = vm.tiles[i]
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(5.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(if (v == 0) Color.Transparent else MaterialTheme.colorScheme.primary)
                                .clickable(enabled = v != 0) { vm.tap(i) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (v == 0) "" else "$v",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        if (vm.finished) {
            Text(
                text = "Hoàn thành!",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
