import instaloader


def download(profile):
    profile = profile.replace(" ","")
    user = instaloader.Instaloader()
    user.save_metadata = False
    user.post_metadata_txt_pattern = ""
    user.dirname_pattern = f"/sdcard/Downloads/{profile}"
    user.download_profile(profile)
