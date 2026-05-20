package com.example.ekhogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ekhogo.ToDo.ToDoClass
import com.example.ekhogo.ToDo.ToDoHomePage
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.clickable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember


@Composable
fun HomeButton(
    onNavigate: (Int) -> Unit,
    toDoList: List<ToDoClass>
) {

    var userName by remember { mutableStateOf("") }
    val firstName = userName.split(" ").firstOrNull() ?: ""
    var classmatesCountInClasses by remember { mutableStateOf(0) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid

        if (uid != null) {
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("name") ?: ""
                    val currentUserClasses = (document.get("classes") as? List<*>)
                        ?.filterIsInstance<String>()
                        ?: emptyList()

                    val currentFriends = (document.get("friends") as? List<*>)
                        ?.filterIsInstance<String>()
                        ?.toSet()
                        ?: emptySet()

                    db.collection("users")
                        .get()
                        .addOnSuccessListener { usersSnapshot ->

                            classmatesCountInClasses = usersSnapshot.documents.count { userDocument ->

                                val otherUserId = userDocument.getString("uid") ?: userDocument.id

                                val otherUserClasses = (userDocument.get("classes") as? List<*>)
                                    ?.filterIsInstance<String>()
                                    ?: emptyList()

                                otherUserId != uid &&
                                        !currentFriends.contains(otherUserId) &&
                                        currentUserClasses.any { currentClass ->
                                            otherUserClasses.contains(currentClass)
                                        }
                            }
                        }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = if(userName.isNotBlank()){
                "Welcome Back, $firstName!"
            } else {
                "Welcome Back!"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ){
            Image(
                painter = painterResource(id = R.drawable.welcome_logo),
                contentDescription = "EkhoGo welcome logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ){
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(235.dp)
                    .clickable { onNavigate(2) },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ){
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "People in your classes",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "$classmatesCountInClasses classmates found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Find students taking similar courses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(235.dp)
                    .clickable { onNavigate(6) },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ){
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "To-Do List",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "To-Do List",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ToDoHomePage(ToDoList = toDoList)

                }
            }
        }
    }
}