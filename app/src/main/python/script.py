import instaloader


def download(profile):
    profile = profile.replace(" ", "")
    user = instaloader.Instaloader()
    user.save_metadata = False
    user.post_metadata_txt_pattern = ""
    user.dirname_pattern = f"/sdcard/InstaLoaderApp/{profile}"
    user.download_profile(profile)


def post_count(username):
    username = username.replace(" ", "")
    L = instaloader.Instaloader()
    profile = instaloader.Profile.from_username(L.context, username)

    posts = profile.get_posts()

    return posts.count