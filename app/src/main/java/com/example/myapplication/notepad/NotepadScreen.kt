package com.example.myapplication.notepad

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.MaterialTheme
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadApp(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf(TextFieldValue("")) }
    val titleInteractionSource = remember { MutableInteractionSource() }
    val contentInteractionSource = remember { MutableInteractionSource() }

    var currentTypingStyle by remember { mutableStateOf(SpanStyle()) }
    var fontSize by remember { mutableIntStateOf(16) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri: Uri? ->
            uri?.let { fileUri ->
                val textToSave = content.annotatedString.text
                scope.launch {
                    val success = saveTextToUri(context, fileUri, textToSave)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            Toast.makeText(context, "File saved!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error saving file.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notepad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    val isBoldActive = currentTypingStyle.fontWeight == FontWeight.Bold
                    IconButton(onClick = {
                        val selection = content.selection

                        currentTypingStyle =
                            if (isBoldActive) {
                                currentTypingStyle.copy(fontWeight = FontWeight.Normal)
                            } else {
                                currentTypingStyle.merge(SpanStyle(fontWeight = FontWeight.Bold))
                            }
                        if (!selection.collapsed) {
                            val newAnnotatedString = buildAnnotatedString {
                                append(content.annotatedString)
                                addStyle(currentTypingStyle, selection.start, selection.end)
                            }
                            content = content.copy(annotatedString = newAnnotatedString)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.FormatBold,
                            contentDescription = "Bold",
                            tint = if (isBoldActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val isItalicActive = currentTypingStyle.fontStyle == FontStyle.Italic
                    IconButton(onClick = {
                        val selection = content.selection

                        currentTypingStyle =
                            if (currentTypingStyle.fontStyle == FontStyle.Italic) {
                                currentTypingStyle.copy(fontStyle = FontStyle.Normal)
                            } else {
                                currentTypingStyle.merge(SpanStyle(fontStyle = FontStyle.Italic))
                            }
                        if (!selection.collapsed) {
                            val newAnnotatedString = buildAnnotatedString {
                                append(content.annotatedString)
                                addStyle(currentTypingStyle, selection.start, selection.end)
                            }
                            content = content.copy(annotatedString = newAnnotatedString)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.FormatItalic,
                            contentDescription = "Italic",
                            tint = if (isItalicActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "$fontSize",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.offset(x = 6.dp)
                    )
                    IconButton(onClick = {
                        val newFontSize = when (fontSize) {
                            16 -> 20
                            20 -> 24
                            24 -> 28
                            else -> 16
                        }
                        fontSize = newFontSize

                        val selection = content.selection
                        val newStyle = SpanStyle(fontSize = newFontSize.sp)

                        currentTypingStyle = currentTypingStyle.merge(newStyle)
                        if (!selection.collapsed) {
                            val newAnnotatedString = buildAnnotatedString {
                                append(content.annotatedString)
                                addStyle(newStyle, selection.start, selection.end)
                            }
                            content = content.copy(annotatedString = newAnnotatedString)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Font Size"
                        )
                    }
                    val fileName = title.ifEmpty { content.annotatedString.text.substringBefore("\n") }
                    IconButton(onClick = { saveFileLauncher.launch("$fileName.txt") }) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save Note"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    title = ""
                    content = TextFieldValue("")
                    currentTypingStyle = SpanStyle()
                    fontSize = 16
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Note")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NotepadField(
                title = title,
                onTitleChange = { title = it },
                content = content,
                onContentChange = { newValue ->
                    val newAnnotatedString = buildAnnotatedString {
                        append(newValue.text)

                        content.annotatedString.spanStyles.forEach { style ->
                            val start = style.start.coerceAtMost(newValue.text.length)
                            val end = style.end.coerceAtMost(newValue.text.length)

                            if (start < end) {
                                addStyle(
                                    style.item,
                                    start,
                                    end
                                )
                            }
                        }

                        val isTyping = newValue.text.length > content.text.length &&
                                newValue.selection.collapsed

                        if (isTyping && currentTypingStyle != SpanStyle()) {
                            val typedRange = TextRange(
                                start = content.selection.start,
                                end = newValue.selection.start
                            )

                            if (typedRange.start <= typedRange.end && typedRange.end <= newValue.text.length) {
                                addStyle(currentTypingStyle, typedRange.start, typedRange.end)
                            }
                        }
                    }
                    content = newValue.copy(annotatedString = newAnnotatedString)
                },
                titleInteractionSource = titleInteractionSource,
                contentInteractionSource = contentInteractionSource
            )
        }
    }
}

@Composable
fun NotepadField(
    title: String,
    onTitleChange: (String) -> Unit,
    content: TextFieldValue,
    onContentChange: (TextFieldValue) -> Unit,
    titleInteractionSource: MutableInteractionSource,
    contentInteractionSource: MutableInteractionSource,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp)
    ) {
        Text(
            text = "Title",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = { Text("Enter title...") },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ),
            singleLine = true,
            interactionSource = titleInteractionSource,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Notes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            TextField(
                value = content,
                onValueChange = onContentChange,
                placeholder = { Text("Start typing your notes...") },
                modifier = Modifier.fillMaxSize(),
                interactionSource = contentInteractionSource,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}

suspend fun saveTextToUri(context: Context, uri: Uri, text: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(text.toByteArray())
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotepadAppPreview() {
    NotepadApp(navController = rememberNavController())
}