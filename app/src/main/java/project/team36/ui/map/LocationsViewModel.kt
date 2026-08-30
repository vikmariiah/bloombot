package project.team36.ui.map


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedLocation

//ViewModel for locations, used by UI where location data is needed
class LocationsViewModel(
    repository: LocationRepository
) : ViewModel() {

    //Listens to locations from Room, Flow ensures that it gets updated automatically
    val savedLocations: StateFlow<List<SavedLocation>> = repository.allLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    //Chosen location
    private val _selectedLocation = MutableStateFlow<SavedLocation?>(null)
    val selectedLocation: StateFlow<SavedLocation?> = _selectedLocation.asStateFlow()

    private val _showMaxLocationsDialog = MutableStateFlow(false)
    val showMaxLocationsDialog: StateFlow<Boolean> = _showMaxLocationsDialog.asStateFlow()

    //Velg location, som kan brukes av dropdown osv
    fun selectLocation(location: SavedLocation) {
        _selectedLocation.value = location
    }


    //to change name of location
    fun updateSelectedLocationName(newName: String) {
        _selectedLocation.update { it?.copy(name = newName) }
    }

    //checks if its possible to add more locations
    fun onAddLocationClicked(): Boolean {
        //if the user already has five locations saved, it returns showmaxlocationsDialog
        return if (savedLocations.value.size >= 5) {
            _showMaxLocationsDialog.value = true
            false
        } else {
            true
        }
    }

    //dismisses the maxLocations dialogbox
    fun dismissMaxLocationsDialog() {
        _showMaxLocationsDialog.value = false
    }
}

class LocationsViewModelFactory(
    private val repository: LocationRepository
) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create (modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown VievModel class")
    }
}