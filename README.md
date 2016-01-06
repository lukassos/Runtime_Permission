# Runtime_Permission
Example of requesting runtime permissions on Android 23 Marshmallow

<b>INSTALL ??</b>:

1. Copy following file into your project : RuntimePermissionsUtils.java

<b>USAGE</b>: 

1. call method from your activity or fragment </br>
<code>
    RuntimePermissionsUtils.request(activityContext, permissions , callback);
</code>

2. verify status in your activity under onRequestPermissionsResult </br>
<code>
    RuntimePermissionsUtils.verify(activityContext, requestCode, callback);
</code>

3. implement callback methods for interaction vith result of your request in PermissionStatus  </br>
   <code> @Override</code> </br>
  <code>  public void onPermissionGranted(RuntimePermissionsUtils.PermissionStatus status) {</code> </br>
   <code>     // show success message</code> </br>
  <code>  }</code>  </br>
<code></code>  </br>
   <code> @Override</code>  </br>
   <code> public void onPermissionDenied(RuntimePermissionsUtils.PermissionStatus status) {</code>  </br>
     <code>   // request it again</code>  </br>
   <code> }</code>  </br>
 <code> </code>  </br>
  
4. if you dont want to be annoying to user, mark twice asked permissions after asking again </br>
   <code> RuntimePermissionsUtils.clearMarkAsAsked(appContext, permissionStringCode);</code>  
    
    
    
THANKS GOES TO : teegarcs
This is fork of : https://github.com/teegarcs/Runtime_Permissions
