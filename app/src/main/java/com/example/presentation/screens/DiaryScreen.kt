package com.example.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.LocalLanguage
import com.example.core.localization.LocalNumeralStyle
import com.example.core.util.CurrencyFormatter
import com.example.core.util.DateUtils
import com.example.data.local.entity.NoteAttachmentEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.presentation.components.PremiumHeader
import com.example.presentation.components.PremiumSearchBar
import com.example.presentation.state.JomaKhorochUiState
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.PlusJakartaSansFont
import com.example.ui.theme.SoftGold
import com.example.ui.theme.WarmGold
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class ChecklistEntry(val text: String, val done: Boolean)

fun parseChecklistJson(json: String): List<ChecklistEntry> = runCatching {
    val arr = JSONArray(json)
    List(arr.length()) { i ->
        val obj = arr.getJSONObject(i)
        ChecklistEntry(obj.optString("text"), obj.optBoolean("done", false))
    }
}.getOrDefault(emptyList())

fun encodeChecklistJson(items: List<ChecklistEntry>): String {
    val arr = JSONArray()
    items.forEach { item ->
        arr.put(JSONObject().apply {
            put("text", item.text)
            put("done", item.done)
        })
    }
    return arr.toString()
}

private enum class DiarySection { NOTES, CHECKLISTS, CONTACTS, REMINDERS }
private enum class DiaryDateFilter { ALL, TODAY, PINNED }

private data class FlattenedChecklistItem(
    val note: NoteEntity,
    val checklist: List<ChecklistEntry>,
    val itemIndex: Int,
    val entry: ChecklistEntry
)

@Composable
fun DiaryScreen(
    uiState: JomaKhorochUiState,
    onAddNoteClick: () -> Unit,
    onEditNoteClick: (NoteEntity) -> Unit,
    onTogglePinNote: (NoteEntity) -> Unit,
    onUpdateChecklist: (NoteEntity, String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onAddTaskClick: () -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onAddNoteWithCategory: ((String) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onOpenNotifications: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    topSwitcher: (@Composable () -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA

    var focusedSectionName by rememberSaveable { mutableStateOf<String?>(null) }
    var dateFilterName by rememberSaveable { mutableStateOf(DiaryDateFilter.ALL.name) }
    var query by rememberSaveable { mutableStateOf("") }
    var showDateFilterMenu by remember { mutableStateOf(false) }

    val focusedSection = remember(focusedSectionName) {
        focusedSectionName?.let { runCatching { DiarySection.valueOf(it) }.getOrNull() }
    }
    val dateFilter = remember(dateFilterName) {
        runCatching { DiaryDateFilter.valueOf(dateFilterName) }.getOrDefault(DiaryDateFilter.ALL)
    }

    val openNoteCreator: (String) -> Unit = { cat ->
        if (onAddNoteWithCategory != null) onAddNoteWithCategory(cat) else onAddNoteClick()
    }

    BackHandler(enabled = focusedSection != null || query.isNotBlank() || onBack != null) {
        when {
            query.isNotBlank() -> query = ""
            focusedSection != null -> focusedSectionName = null
            else -> onBack?.invoke()
        }
    }

    val now = remember { System.currentTimeMillis() }
    val todayStart = remember(now) { DateUtils.startOfDay(now) }
    val todayEnd = remember(now) { DateUtils.endOfDay(now) }
    val q = query.trim().lowercase()

    val filteredNotes = remember(uiState.notes, q, dateFilter, todayStart, todayEnd) {
        uiState.notes.filter { note ->
            val dateOk = when (dateFilter) {
                DiaryDateFilter.ALL -> true
                DiaryDateFilter.TODAY -> note.updatedAt in todayStart..todayEnd || note.createdAt in todayStart..todayEnd
                DiaryDateFilter.PINNED -> note.isPinned
            }
            if (!dateOk) return@filter false
            if (q.isEmpty()) true
            else note.title.lowercase().contains(q) ||
                note.content.lowercase().contains(q) ||
                note.contactPhone.lowercase().contains(q) ||
                note.contactAddress.lowercase().contains(q)
        }
    }

    val generalNotes = remember(filteredNotes) {
        filteredNotes.filter { it.category != "SHOPPING_LIST" && it.category != "PHONE_ADDRESS" }
    }
    val checklistNotes = remember(filteredNotes) {
        filteredNotes.filter { it.category == "SHOPPING_LIST" || parseChecklistJson(it.checklistJson).isNotEmpty() }
    }
    val flattenedChecklists = remember(checklistNotes) {
        checklistNotes.flatMap { note ->
            val list = parseChecklistJson(note.checklistJson)
            if (list.isEmpty()) {
                listOf(
                    FlattenedChecklistItem(
                        note = note,
                        checklist = listOf(ChecklistEntry(note.title, false)),
                        itemIndex = 0,
                        entry = ChecklistEntry(note.title, false)
                    )
                )
            } else {
                list.mapIndexed { idx, entry ->
                    FlattenedChecklistItem(note, list, idx, entry)
                }
            }
        }
    }
    val contactNotes = remember(filteredNotes) {
        filteredNotes.filter {
            it.category == "PHONE_ADDRESS" || it.contactPhone.isNotBlank() || it.contactAddress.isNotBlank()
        }
    }
    val filteredTasks = remember(uiState.tasks, q, dateFilter, todayStart, todayEnd) {
        uiState.tasks.filter { task ->
            val dateOk = when (dateFilter) {
                DiaryDateFilter.ALL -> true
                DiaryDateFilter.TODAY -> (task.dueAt ?: task.createdAt) in todayStart..todayEnd
                DiaryDateFilter.PINNED -> !task.isCompleted
            }
            if (!dateOk) return@filter false
            if (q.isEmpty()) true
            else task.title.lowercase().contains(q) || task.description.lowercase().contains(q)
        }
    }

    val dateFilterLabel = when (dateFilter) {
        DiaryDateFilter.ALL -> if (isBn) "সব" else "All"
        DiaryDateFilter.TODAY -> if (isBn) "আজ" else "Today"
        DiaryDateFilter.PINNED -> if (isBn) "পিন করা" else "Pinned"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PremiumHeader(
            onBackClick = onBack,
            notificationCount = uiState.notifications.size,
            hasUrgentNotification = uiState.notifications.any { it.isUrgent },
            onNotificationClick = onOpenNotifications,
            onSettingsClick = onOpenSettings
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (topSwitcher != null) {
                item(key = "diary_top_switcher") {
                    topSwitcher()
                }
            }

            // 1. Page Title Row + Date Filter Pill ("আজ ∨" / "সব ∨")
            item(key = "diary_header_title") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(
                                    if (finance.isDark) MaterialTheme.colorScheme.primaryContainer else DeepEmerald
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EditNote,
                                contentDescription = null,
                                tint = if (finance.isDark) MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "পকেট ডায়রি ও রিমাইন্ডার" else "Pocket Diary & Reminders",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isBn) "নোট, তালিকা, যোগাযোগ ও গুরুত্বপূর্ণ স্মরণিকা" else "Notes, checklists, contacts & reminders",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Box {
                        Surface(
                            onClick = { showDateFilterMenu = true },
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shadowElevation = 1.dp,
                            modifier = Modifier.testTag("diary_date_filter_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = dateFilterLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showDateFilterMenu,
                            onDismissRequest = { showDateFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isBn) "সব আইটেম" else "All Items") },
                                onClick = {
                                    dateFilterName = DiaryDateFilter.ALL.name
                                    showDateFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isBn) "আজ" else "Today") },
                                onClick = {
                                    dateFilterName = DiaryDateFilter.TODAY.name
                                    showDateFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isBn) "পিন করা / বকেয়া" else "Pinned / Pending") },
                                onClick = {
                                    dateFilterName = DiaryDateFilter.PINNED.name
                                    showDateFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // 2. Four Pastel Quick-Action Tiles matching the mockup
            item(key = "diary_pastel_tiles") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DiaryPastelActionTile(
                        label = if (isBn) "নতুন নোট" else "New Note",
                        icon = Icons.Filled.NoteAdd,
                        containerColor = if (finance.isDark) Color(0xFF153124) else Color(0xFFE7F6EC),
                        borderColor = if (finance.isDark) Color(0xFF285C43) else Color(0xFFBBE4CA),
                        iconTint = if (finance.isDark) Color(0xFF5CE1A3) else Color(0xFF0A6B41),
                        onClick = { openNoteCreator("PERSONAL") },
                        tag = "add_note_button",
                        modifier = Modifier.weight(1f)
                    )
                    DiaryPastelActionTile(
                        label = if (isBn) "চেকলিস্ট" else "Checklist",
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        containerColor = if (finance.isDark) Color(0xFF332818) else Color(0xFFFDF2DF),
                        borderColor = if (finance.isDark) Color(0xFF5B4526) else Color(0xFFF3D6A8),
                        iconTint = if (finance.isDark) Color(0xFFF3BD65) else Color(0xFFC47A16),
                        onClick = { openNoteCreator("SHOPPING_LIST") },
                        tag = "add_checklist_button",
                        modifier = Modifier.weight(1f)
                    )
                    DiaryPastelActionTile(
                        label = if (isBn) "যোগাযোগ" else "Contact",
                        icon = Icons.Filled.PersonAdd,
                        containerColor = if (finance.isDark) Color(0xFF162B3D) else Color(0xFFE7F3FE),
                        borderColor = if (finance.isDark) Color(0xFF2A4D6D) else Color(0xFFBADCF9),
                        iconTint = if (finance.isDark) Color(0xFF6BB4F8) else Color(0xFF1A78CF),
                        onClick = { openNoteCreator("PHONE_ADDRESS") },
                        tag = "add_contact_button",
                        modifier = Modifier.weight(1f)
                    )
                    DiaryPastelActionTile(
                        label = if (isBn) "রিমাইন্ডার" else "Reminder",
                        icon = Icons.Filled.Alarm,
                        containerColor = if (finance.isDark) Color(0xFF2B1C3D) else Color(0xFFF4EAFF),
                        borderColor = if (finance.isDark) Color(0xFF4F3170) else Color(0xFFDCC3FA),
                        iconTint = if (finance.isDark) Color(0xFFC68BFF) else Color(0xFF7B21D9),
                        onClick = onAddTaskClick,
                        tag = "add_task_button",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Focused section back + search bar (when user taps "সব দেখুন →" on any section)
            if (focusedSection != null) {
                item(key = "focused_section_bar") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = {
                                    focusedSectionName = null
                                    query = ""
                                },
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isBn) "সব সেকশনে ফিরুন" else "Back to Overview",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        PremiumSearchBar(
                            query = query,
                            onQueryChange = { query = it },
                            placeholder = strings.searchNotes,
                            inputTag = "diary_search_input",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 3. Section 1: আমার নোট (My Notes)
            if (focusedSection == null || focusedSection == DiarySection.NOTES) {
                item(key = "section_my_notes") {
                    val displayedNotes = if (focusedSection == DiarySection.NOTES) generalNotes else generalNotes.take(3)
                    DiarySectionCard(
                        icon = Icons.Filled.Description,
                        iconTint = SoftGold,
                        title = if (isBn) "আমার নোট" else "My Notes",
                        actionLabel = if (focusedSection == DiarySection.NOTES) {
                            "+ ${if (isBn) "নতুন" else "Add"}"
                        } else {
                            if (isBn) "সব দেখুন" else "See All"
                        },
                        showArrow = focusedSection != DiarySection.NOTES,
                        onActionClick = {
                            if (focusedSection == DiarySection.NOTES) openNoteCreator("PERSONAL")
                            else focusedSectionName = DiarySection.NOTES.name
                        }
                    ) {
                        if (displayedNotes.isEmpty()) {
                            DiaryEmptyInlineRow(
                                message = strings.noNotesYet,
                                actionLabel = if (isBn) "নতুন নোট লিখুন" else "Write a Note",
                                onAction = { openNoteCreator("PERSONAL") }
                            )
                        } else {
                            displayedNotes.forEachIndexed { idx, note ->
                                MockupNoteRow(
                                    note = note,
                                    attachments = uiState.attachmentsByNoteId[note.id].orEmpty(),
                                    onClick = { onEditNoteClick(note) },
                                    onTogglePin = { onTogglePinNote(note) },
                                    onDelete = { onDeleteNote(note.id) }
                                )
                                if (idx < displayedNotes.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Section 2: আমার চেকলিস্ট (My Checklists)
            if (focusedSection == null || focusedSection == DiarySection.CHECKLISTS) {
                item(key = "section_my_checklists") {
                    val displayedItems = if (focusedSection == DiarySection.CHECKLISTS) {
                        flattenedChecklists
                    } else {
                        flattenedChecklists.take(4)
                    }
                    DiarySectionCard(
                        icon = Icons.Filled.CheckBox,
                        iconTint = Color.White,
                        title = if (isBn) "আমার চেকলিস্ট" else "My Checklists",
                        actionLabel = if (focusedSection == DiarySection.CHECKLISTS) {
                            "+ ${if (isBn) "নতুন" else "Add"}"
                        } else {
                            if (isBn) "সব দেখুন" else "See All"
                        },
                        showArrow = focusedSection != DiarySection.CHECKLISTS,
                        onActionClick = {
                            if (focusedSection == DiarySection.CHECKLISTS) openNoteCreator("SHOPPING_LIST")
                            else focusedSectionName = DiarySection.CHECKLISTS.name
                        }
                    ) {
                        if (displayedItems.isEmpty()) {
                            DiaryEmptyInlineRow(
                                message = if (isBn) "কোনো বাজার বা কাজের চেকলিস্ট নেই" else "No checklist items yet",
                                actionLabel = if (isBn) "চেকলিস্ট তৈরি করুন" else "Add Checklist",
                                onAction = { openNoteCreator("SHOPPING_LIST") }
                            )
                        } else {
                            displayedItems.forEachIndexed { idx, item ->
                                MockupChecklistRow(
                                    item = item,
                                    todayStart = todayStart,
                                    todayEnd = todayEnd,
                                    onToggle = {
                                        val updated = item.checklist.toMutableList()
                                        if (item.itemIndex in updated.indices) {
                                            updated[item.itemIndex] = item.entry.copy(done = !item.entry.done)
                                            onUpdateChecklist(item.note, encodeChecklistJson(updated))
                                        }
                                    },
                                    onOpenNote = { onEditNoteClick(item.note) }
                                )
                                if (idx < displayedItems.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Section 3: আমার গুরুত্বপূর্ণ যোগাযোগ (My Important Contacts)
            if (focusedSection == null || focusedSection == DiarySection.CONTACTS) {
                item(key = "section_my_contacts") {
                    DiarySectionCard(
                        icon = Icons.Filled.Groups,
                        iconTint = Color.White,
                        title = if (isBn) "আমার গুরুত্বপূর্ণ যোগাযোগ" else "Important Contacts",
                        actionLabel = if (focusedSection == DiarySection.CONTACTS) {
                            "+ ${if (isBn) "যোগ করুন" else "Add"}"
                        } else {
                            if (isBn) "সব দেখুন" else "See All"
                        },
                        showArrow = focusedSection != DiarySection.CONTACTS,
                        onActionClick = {
                            if (focusedSection == DiarySection.CONTACTS) openNoteCreator("PHONE_ADDRESS")
                            else focusedSectionName = DiarySection.CONTACTS.name
                        }
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            items(contactNotes, key = { "contact_avatar_${it.id}" }) { contact ->
                                MockupContactAvatarTile(
                                    note = contact,
                                    attachment = uiState.attachmentsByNoteId[contact.id]?.firstOrNull(),
                                    onClick = { onEditNoteClick(contact) }
                                )
                            }
                            item(key = "add_contact_tile") {
                                Surface(
                                    onClick = { openNoteCreator("PHONE_ADDRESS") },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (finance.isDark) Color(0xFF173628) else Color(0xFFE5F4EA),
                                    border = BorderStroke(
                                        1.dp,
                                        if (finance.isDark) Color(0xFF2B5F47) else Color(0xFFC2E5CE)
                                    ),
                                    modifier = Modifier
                                        .width(96.dp)
                                        .height(114.dp)
                                        .testTag("contacts_inline_add_tile")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = if (isBn) "যোগাযোগ যোগ করুন" else "Add Contact",
                                            tint = if (finance.isDark) Color(0xFF5EE3A5) else DeepEmerald,
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = if (isBn) "যোগাযোগ\nযোগ করুন" else "Add\nContact",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (finance.isDark) Color(0xFFD6F5E5) else DeepEmerald,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Section 4: আমার রিমাইন্ডার (My Reminders)
            if (focusedSection == null || focusedSection == DiarySection.REMINDERS) {
                item(key = "section_my_reminders") {
                    val sortedTasks = remember(filteredTasks) {
                        filteredTasks.sortedBy { it.isCompleted }
                    }
                    val displayedTasks = if (focusedSection == DiarySection.REMINDERS) sortedTasks else sortedTasks.take(4)
                    DiarySectionCard(
                        icon = Icons.Filled.Notifications,
                        iconTint = SoftGold,
                        title = if (isBn) "আমার রিমাইন্ডার" else "My Reminders",
                        actionLabel = if (focusedSection == DiarySection.REMINDERS) {
                            "+ ${if (isBn) "নতুন" else "Add"}"
                        } else {
                            if (isBn) "সব দেখুন" else "See All"
                        },
                        showArrow = focusedSection != DiarySection.REMINDERS,
                        onActionClick = {
                            if (focusedSection == DiarySection.REMINDERS) onAddTaskClick()
                            else focusedSectionName = DiarySection.REMINDERS.name
                        }
                    ) {
                        if (displayedTasks.isEmpty()) {
                            DiaryEmptyInlineRow(
                                message = strings.noTasksYet,
                                actionLabel = if (isBn) "রিমাইন্ডার সেট করুন" else "Add Reminder",
                                onAction = onAddTaskClick
                            )
                        } else {
                            displayedTasks.forEachIndexed { idx, task ->
                                MockupReminderRow(
                                    task = task,
                                    onToggle = { onToggleTask(task) },
                                    onDelete = { onDeleteTask(task.id) }
                                )
                                if (idx < displayedTasks.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryPastelActionTile(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    borderColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(82.dp)
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.2.dp, borderColor),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Connected Deep Emerald section banner + clean white card container matching the "আরও" mockup.
 */
@Composable
private fun DiarySectionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    actionLabel: String,
    showArrow: Boolean,
    onActionClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val finance = LocalFinanceColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Deep Emerald Header Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(finance.sectionBannerBg, DeepEmerald)
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(21.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        if (showArrow) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Connected White Card Body
            content()
        }
    }
}

@Composable
private fun DiaryEmptyInlineRow(
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = onAction) {
            Text(actionLabel, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MockupNoteRow(
    note: NoteEntity,
    attachments: List<NoteAttachmentEntity>,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val isBn = language == AppLanguage.BANGLA
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Warm Gold Sticky-Note Visual Graphic with Green Pushpin
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.BottomStart)
            ) {
                drawRoundRect(
                    color = Color(0xFFFCD97B),
                    cornerRadius = CornerRadius(10.dp.toPx())
                )
                val lineColor = Color(0xFFD1932C).copy(alpha = 0.72f)
                val startX = 10.dp.toPx()
                val lineH = 3.2.dp.toPx()
                drawRoundRect(
                    color = lineColor,
                    topLeft = Offset(startX, 13.dp.toPx()),
                    size = Size(size.width * 0.56f, lineH),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                drawRoundRect(
                    color = lineColor,
                    topLeft = Offset(startX, 23.dp.toPx()),
                    size = Size(size.width * 0.66f, lineH),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                drawRoundRect(
                    color = lineColor,
                    topLeft = Offset(startX, 33.dp.toPx()),
                    size = Size(size.width * 0.44f, lineH),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
            if (note.isPinned || note.category == "IMPORTANT") {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (note.isPinned) Color(0xFF1E9B4B) else Color(0xFFD14343)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = strings.pinNote,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (attachments.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(attachments.take(3), key = { it.id }) { att ->
                        AsyncImage(
                            model = File(att.filePath),
                            contentDescription = att.caption.ifBlank { note.title },
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = DateUtils.formatDateTime(note.updatedAt, language, numerals),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = if (isBn) "অপশন" else "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            if (note.isPinned) (if (isBn) "আনপিন করুন" else "Unpin")
                            else strings.pinNote
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.PushPin, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    onClick = {
                        menuExpanded = false
                        onTogglePin()
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (isBn) "সম্পাদনা করুন" else "Edit") },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    onClick = {
                        menuExpanded = false
                        onClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text(strings.delete, color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun MockupChecklistRow(
    item: FlattenedChecklistItem,
    todayStart: Long,
    todayEnd: Long,
    onToggle: () -> Unit,
    onOpenNote: () -> Unit
) {
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA
    val isDone = item.entry.done

    val badgeLabel = remember(item.note.updatedAt, todayStart, todayEnd, language, numerals) {
        if (item.note.updatedAt in todayStart..todayEnd) {
            if (isBn) "আজ" else "Today"
        } else {
            DateUtils.formatDate(item.note.updatedAt, language, numerals)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Emerald Checkbox adaptive to Light/Dark mode
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isDone) MaterialTheme.colorScheme.primary else Color.Transparent)
                .border(
                    width = 1.6.dp,
                    color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Icon(
            imageVector = Icons.Filled.ShoppingCart,
            contentDescription = null,
            tint = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            modifier = Modifier.size(19.dp)
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = item.entry.text,
            style = MaterialTheme.typography.titleSmall.copy(
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
            ),
            fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold,
            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        Surface(
            onClick = onOpenNote,
            shape = RoundedCornerShape(50),
            color = finance.incomeContainer
        ) {
            Text(
                text = badgeLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = finance.income,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun MockupContactAvatarTile(
    note: NoteEntity,
    attachment: NoteAttachmentEntity?,
    onClick: () -> Unit
) {
    val finance = LocalFinanceColors.current
    val initial = note.title.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val avatarColors = listOf(
        DeepEmerald to SoftGold,
        Color(0xFF1E5D8C) to Color.White,
        Color(0xFF8C531E) to Color(0xFFFFF3D4),
        Color(0xFF5D2D7A) to Color.White
    )
    val (bg, fg) = avatarColors[(note.id.toInt().and(Int.MAX_VALUE)) % avatarColors.size]

    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (attachment != null && File(attachment.filePath).exists()) {
            AsyncImage(
                model = File(attachment.filePath),
                contentDescription = note.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, WarmGold.copy(alpha = 0.65f), CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(bg)
                    .border(1.5.dp, WarmGold.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = PlusJakartaSansFont,
                        fontWeight = FontWeight.Bold
                    ),
                    color = fg
                )
            }
        }

        Text(
            text = note.title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        if (note.contactPhone.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Phone,
                    contentDescription = null,
                    tint = finance.income,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = note.contactPhone,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 10.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MockupReminderRow(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA
    var menuExpanded by remember { mutableStateOf(false) }

    val (icon, iconBg, iconTint) = when (task.taskType) {
        "BILL" -> Triple(
            Icons.Filled.CalendarMonth,
            if (finance.isDark) Color(0xFF2D1C42) else Color(0xFFF3E7FE),
            if (finance.isDark) Color(0xFFC58BFF) else Color(0xFF7B21D9)
        )
        "LOAN_REPAYMENT" -> Triple(
            Icons.Filled.Handshake,
            finance.goldContainer,
            WarmGold
        )
        "CUSTOM" -> Triple(
            Icons.Filled.CardGiftcard,
            if (finance.isDark) Color(0xFF3A1F1B) else Color(0xFFFDECE6),
            if (finance.isDark) Color(0xFFFF8E7A) else Color(0xFFD93B2B)
        )
        else -> Triple(
            Icons.Filled.EventNote,
            finance.incomeContainer,
            finance.income
        )
    }

    val (pillLabel, pillBg, pillFg) = when {
        task.isCompleted -> Triple(
            if (isBn) "সম্পন্ন" else "Done",
            finance.incomeContainer,
            finance.income
        )
        task.taskType == "BILL" || task.taskType == "LOAN_REPAYMENT" -> Triple(
            if (isBn) "গুরুত্বপূর্ণ" else "Important",
            if (finance.isDark) Color(0xFF3E1C1A) else Color(0xFFFDE7E3),
            if (finance.isDark) Color(0xFFFF9080) else Color(0xFFC93D2B)
        )
        else -> Triple(
            if (isBn) "স্মরণিকা" else "Reminder",
            finance.receivableContainer,
            finance.receivable
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                ),
                fontWeight = FontWeight.Bold,
                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val timeStamp = task.reminderAt ?: task.dueAt
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = timeStamp?.let { DateUtils.formatDateTime(it, language, numerals) }
                        ?: DateUtils.formatDate(task.createdAt, language, numerals),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(50),
            color = pillBg
        ) {
            Text(
                text = pillLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = pillFg,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = if (isBn) "অপশন" else "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            if (task.isCompleted) (if (isBn) "বাকি হিসেবে চিহ্নিত করুন" else "Mark Pending")
                            else (if (isBn) "সম্পন্ন করুন" else "Mark Done")
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onToggle()
                    }
                )
                DropdownMenuItem(
                    text = { Text(strings.delete, color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteSheet(
    existing: NoteEntity?,
    existingAttachments: List<NoteAttachmentEntity> = emptyList(),
    initialCategory: String = "PERSONAL",
    onDeleteAttachment: (NoteAttachmentEntity) -> Unit = {},
    onSave: (
        existingId: Long,
        existingCreatedAt: Long,
        title: String,
        content: String,
        category: String,
        checklistJson: String,
        phone: String,
        address: String,
        isPinned: Boolean,
        pendingPhotoUris: List<Uri>
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var content by remember { mutableStateOf(existing?.content ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: initialCategory) }
    var phone by remember { mutableStateOf(existing?.contactPhone ?: "") }
    var address by remember { mutableStateOf(existing?.contactAddress ?: "") }
    var isPinned by remember { mutableStateOf(existing?.isPinned ?: false) }
    val checklist = remember {
        mutableStateListOf<ChecklistEntry>().apply {
            existing?.let { addAll(parseChecklistJson(it.checklistJson)) }
        }
    }
    var newChecklistItem by remember { mutableStateOf("") }
    val pendingPhotos = remember { mutableStateListOf<Uri>() }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Zero-permission Android Photo Picker (Play Policy compliant)
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { pendingPhotos.add(it) }
    }

    val noteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = noteSheetState,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(strings.addNote, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            val catOptions = listOf(
                "PERSONAL" to strings.personalNote,
                "SHOPPING_LIST" to strings.shoppingList,
                "PHONE_ADDRESS" to strings.phoneAddress,
                "IMPORTANT" to strings.importantInfo
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(catOptions) { (key, label) ->
                    FilterChip(
                        selected = category == key,
                        onClick = { category = key },
                        label = { Text(label) }
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; errorMsg = null },
                label = { Text(strings.noteTitle) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input")
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(strings.noteContent) },
                minLines = 2,
                maxLines = 5,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (category == "PHONE_ADDRESS") {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(strings.phoneNumber) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(strings.address) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (category == "SHOPPING_LIST") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newChecklistItem,
                        onValueChange = { newChecklistItem = it },
                        label = { Text(strings.addChecklistItem) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newChecklistItem.isNotBlank()) {
                                checklist.add(ChecklistEntry(newChecklistItem.trim(), false))
                                newChecklistItem = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Icon(Icons.Filled.Add, contentDescription = null) }
                }
                checklist.forEachIndexed { idx, entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• ${entry.text}", style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = { checklist.removeAt(idx) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (existingAttachments.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(existingAttachments, key = { it.id }) { att ->
                        Box {
                            AsyncImage(
                                model = File(att.filePath),
                                contentDescription = att.caption.ifBlank { title },
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            IconButton(
                                onClick = { onDeleteAttachment(att) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = strings.delete, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${strings.attachPhoto} (${existingAttachments.size + pendingPhotos.size})")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.pinNote, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(6.dp))
                    Switch(checked = isPinned, onCheckedChange = { isPinned = it })
                }
            }

            errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMsg = strings.requiredFieldError
                        return@Button
                    }
                    // If user typed a checklist item without tapping "+", auto-include it
                    if (category == "SHOPPING_LIST" && newChecklistItem.isNotBlank()) {
                        checklist.add(ChecklistEntry(newChecklistItem.trim(), false))
                        newChecklistItem = ""
                    }
                    onSave(
                        existing?.id ?: 0L,
                        existing?.createdAt ?: System.currentTimeMillis(),
                        title,
                        content,
                        category,
                        encodeChecklistJson(checklist),
                        phone,
                        address,
                        isPinned,
                        pendingPhotos.toList()
                    )
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_note_button")
            ) { Text(strings.save, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    onSave: (title: String, description: String, taskType: String, dueAt: Long?, reminderAt: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var taskType by remember { mutableStateOf("DAILY") }
    var dueDateMillis by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var enableReminder by remember { mutableStateOf(false) }
    var reminderOffsetHours by remember { mutableIntStateOf(2) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        enableReminder = granted
    }

    val taskSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = taskSheetState,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(strings.addTask, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            val types = listOf(
                "DAILY" to strings.dailyTask,
                "BILL" to strings.billReminder,
                "LOAN_REPAYMENT" to strings.loanReminder,
                "CUSTOM" to strings.customReminder
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(types) { (key, label) ->
                    FilterChip(selected = taskType == key, onClick = { taskType = key }, label = { Text(label) })
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; errorMsg = null },
                label = { Text(strings.taskTitle) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input")
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(strings.description) },
                maxLines = 2,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "${strings.dueDate}: ${
                        dueDateMillis?.let { DateUtils.formatDate(it, language, numerals) } ?: "-"
                    }"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.enableReminder, style = MaterialTheme.typography.titleSmall)
                Switch(
                    checked = enableReminder,
                    onCheckedChange = { checked ->
                        if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                enableReminder = true
                            } else {
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            enableReminder = checked
                        }
                    }
                )
            }

            if (enableReminder) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 3, 24).forEach { hours ->
                        val chipLabel = if (language == AppLanguage.BANGLA) {
                            "+${CurrencyFormatter.toLocalizedDigits(hours.toString(), numerals)} ঘণ্টা"
                        } else {
                            "+${hours}h"
                        }
                        FilterChip(
                            selected = reminderOffsetHours == hours,
                            onClick = { reminderOffsetHours = hours },
                            label = { Text(chipLabel) }
                        )
                    }
                }
            }

            errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMsg = strings.requiredFieldError
                        return@Button
                    }
                    val now = System.currentTimeMillis()
                    val reminderTime = if (enableReminder) now + reminderOffsetHours * 3_600_000L else null
                    onSave(
                        title,
                        description,
                        taskType,
                        dueDateMillis,
                        reminderTime
                    )
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_task_button")
            ) { Text(strings.save, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(18.dp))
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDateMillis = state.selectedDateMillis
                    showDatePicker = false
                }) { Text(strings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(strings.cancel) }
            }
        ) { DatePicker(state = state) }
    }
}
