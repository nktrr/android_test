package com.example.rick

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.example.rick.ui.theme.RickTheme
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import java.lang.Exception
import java.util.function.Consumer


class MainActivity : ComponentActivity() {
    private val viewModel: CharacterViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RickTheme {
                // A surface container using the 'background' color from the theme
                Surface(
//                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Main(viewModel)
//                    Search()
//                    CharacterScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun Main(viewModel: CharacterViewModel){
    Column {
        Row(modifier = Modifier.height(60.dp), verticalAlignment = Alignment.CenterVertically) {
            Search()
            Text(text = "FILTER")
        }
        CharacterScreen(viewModel = viewModel)
    }
}

@Preview
@Composable
fun CharacterBox(@PreviewParameter(CharacterProvider::class) character: Character){
    Box(modifier = Modifier.background(color = Color.White).border(5.dp, Color.Black, shape = RoundedCornerShape(5.dp)).padding(4.dp).height(200.dp).width(150.dp).fillMaxWidth(), contentAlignment = Alignment.Center){
        Column(modifier = Modifier.fillMaxWidth().background(Color.White), horizontalAlignment = Alignment.CenterHorizontally,) {
            AsyncImage(
                model = character.image,
                contentDescription = null,
            )
//            Image(
//                modifier = Modifier.width(100.dp).height(100.dp),
//                painter = rememberImagePainter(
//                    data = "https://www.example.com/image.jpg",
//                    builder = {
//                        placeholder(R.drawable.rick)
//                    }
//                ),
//                contentDescription = "some description",
//            )
            Text(text = character.name, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            Row (modifier = Modifier.fillMaxWidth(0.8f),horizontalArrangement = Arrangement.SpaceBetween){
                Text(text = "Status", fontSize = 8.sp)
                Text(text = "Gender", fontSize = 8.sp)
            }
            Row (modifier = Modifier.fillMaxWidth(0.8f),horizontalArrangement = Arrangement.SpaceBetween){
                Text(text = character.status)
                Text(text = character.gender)
            }
            Text(text = "Species", fontSize = 8.sp)
            Text(text = character.species)
        }
    }
}

class CharacterProvider: PreviewParameterProvider<Character>{
    override val values = sequenceOf(Character(0, "Rick Sanchez", "Alive", "Human", "", "Male", null, null, "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
    , arrayListOf<String>("",""),"","",))

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search() {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
//        modifier = Modifier
//            .width(300.dp)
//            .height(80.dp),
        value = text,
        onValueChange = { text = it },
        label = { Text("Search by name", style = TextStyle(color = Color.Black)) }
    )
}




interface RMService{
    @GET("character/")
    suspend fun getCharacters(): CharactersDto
}


object RetrofitInstance{
    private const val BASE_URL = "https://rickandmortyapi.com/api/"

    private val retrofit: Retrofit by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val rmService: RMService by lazy{
        retrofit.create(RMService::class.java)
    }
}

class RMRepository{
    private val rmService = RetrofitInstance.rmService

    suspend fun getCharacters(): CharactersDto{
        return rmService.getCharacters()
    }
}

class CharacterViewModel : ViewModel(){
    private val repository = RMRepository()

    private val _characters = MutableLiveData<List<Character>>()
    val characters: LiveData<List<Character>> = _characters

    fun fetchCharacters() {
        viewModelScope.launch {
            try{
                val charactersDto = repository.getCharacters()
                _characters.value = charactersDto.results
            } catch (e: Exception){
                e.message?.let { Log.e("FETCH_CHARACTERS", it) }
            }
        }
    }
}

@Composable
fun CharacterScreen(viewModel: CharacterViewModel) {
    val characters by viewModel.characters. observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchCharacters()
    }

    Column {
        if (characters.isEmpty()) {
            // Show loading indicator or placeholder
            Text(text = "Loading...")
        } else {
            // Display the list of credit cards
            LazyColumn {
                items(characters) { character ->
                    CharacterBox(character = character)
//                    Text(text = character.name)
//                    Text(text = character.created)
//                    Text(text = character.gender)
                    Divider() // Add a divider between items
                }
            }
        }
    }
}


//class Controller : Callback<CharactersDto>{
//    companion object {
//        const val BASE_URL = "https://rickandmortyapi.com/api/"
//    }
//    fun start(){
//        val gson = GsonBuilder()
//            .setLenient()
//            .create()
//        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .build()
//        val rickAPI: RickMortyAPI = retrofit.create(RickMortyAPI::class.java)
//        val call: Call<CharactersDto> = rickAPI.getCharacters()
//        call.enqueue(this)
//    }
//
//    override fun onResponse(call: Call<CharactersDto>, response: Response<CharactersDto>) {
//        Log.d("RESPONSE", "response")
//        Log.d("REQUEST_URL", call.request().url().toString())
//        if (response.isSuccessful){
//            val charactersDto: CharactersDto? = response.body()
//            charactersDto?.results?.forEach(Consumer {character -> character.name?.let {
//                Log.d("Character",
//                    it
//                )
//            } })
//        }
//        else{
//            Log.d("RESPONSE_ERROR", response.errorBody().toString())
//        }
//    }
//
//    override fun onFailure(call: Call<CharactersDto>, t: Throwable) {
//        Log.e("REQUEST_ERR", t.message.toString())
//    }
//}





data class CharactersDto(
    @SerializedName("info") var info : Info? = Info(),
    @SerializedName("results") var results: ArrayList<Character> = arrayListOf()
)

data class Info (
    @SerializedName("count" ) var count : Int?    = null,
    @SerializedName("pages" ) var pages : Int?    = null,
    @SerializedName("next"  ) var next  : String? = null,
    @SerializedName("prev"  ) var prev  : String? = null
)

data class Character (
    @SerializedName("id"       ) var id       : Int?              = null,
    @SerializedName("name"     ) var name     : String,
    @SerializedName("status"   ) var status   : String,
    @SerializedName("species"  ) var species  : String,
    @SerializedName("type"     ) var type     : String?           = null,
    @SerializedName("gender"   ) var gender   : String,
    @SerializedName("origin"   ) var origin   : Origin?           = Origin(),
    @SerializedName("location" ) var location : Location?         = Location(),
    @SerializedName("image"    ) var image    : String?           = null,
    @SerializedName("episode"  ) var episode  : ArrayList<String> = arrayListOf(),
    @SerializedName("url"      ) var url      : String?           = null,
    @SerializedName("created"  ) var created  : String
)

data class Location (
    @SerializedName("name" ) var name : String? = null,
    @SerializedName("url"  ) var url  : String? = null
)

data class Origin (
    @SerializedName("name" ) var name : String? = null,
    @SerializedName("url"  ) var url  : String? = null
)