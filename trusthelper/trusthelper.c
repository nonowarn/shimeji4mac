#include <Carbon/Carbon.h>

const CFStringRef exePath = CFSTR("../../MacOS/JavaApplicationStub");

int main(int argc, char *argv[])
{
  OSStatus err;
  FSRef app;
  CFURLRef appUrl;

  sleep(1); // Ensure caller app has exited

  if (AXMakeProcessTrusted(exePath) != kAXErrorSuccess) {
    fprintf(stderr, "There's something wrong...\n");
  }
  err = FSPathMakeRef(
    "../../../../Shimeji.app",
    &app,
    NULL);

  if (err != noErr) {
    fprintf(stderr, "Can't find the application\n");
    exit(1);
  }

  LSApplicationParameters params = {0, kLSLaunchDefaults, &app, NULL, NULL, NULL, NULL};
    
  err = LSOpenApplication(&params, NULL);
  if (err != noErr) {
    fprintf(stderr, "Cant open the application\n");
    exit(2);
  }

  return 0;
}
