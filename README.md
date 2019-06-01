# Track Containers

Track Container is an Android app to track status and locations of waste containers in the city. It also updates location of them.

After login, on main screen, 1000 of selected containers are displayed according to current zoom factor of map. Since it would be too complex to see all containers suddenly for a user, it is limited to a number in that way. As user zooms into a point, all containers are loaded to zoomed area. 

Users can display container information detail by simply clicking on marker. All needed information is given on an Alert Dialog. Also relocation option for a container is given on this dialog to user.

#### Container Information
 All container information is kept on database on Firebase. Firebase Realtime Database service is used.
 
 4000 container is created on this database.
 
#### User Information
 User information is kept on Firebase Auth service. Users can login with account given below:
 
 E-mail: husamettinor@gmail.com
 Password: 123456

#### Data Update
Any data change on container database on Firebase triggers an event that is sent to all active Android applications and locations of containers are updated. In this way, a user can observe the change done by another user immediately.

MapView is used for location update of containers. After clicking Relocate option on Container Detail dialog, a map is shown with the last known location of the device. User can pick any place on the map and assign as new location of the selected map.


