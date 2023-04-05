import instaloader


def download(profile):
    user = instaloader.Instaloader()
    user.dirname_pattern = f"sdcard/InstaLoader/{profile}/"
    user.download_profile(profile)
    print("\n\nDownload Completed.\n\n")