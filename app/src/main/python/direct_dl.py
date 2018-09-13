import youtube_dl,shutil,os,subprocess,sys,importlib
sys.path.append('/sdcard')
act = ''
flag = False
video = False

def download(acti,link,store,type,ffmpeg):
    global act,flag,video
    flag = True
    act = acti
    # copy()
    if (store[-1:] == '/'):
        store = store[:-1]
    if type=="video":
        video = True
        ydl = youtube_dl.YoutubeDL({
            'outtmpl': store+'/%(title)s.%(ext)s',
            'writeautomaticsub':True,
            'writesubtitles':True,
            'format':'bestvideo/mkv',
            'subtitleslangs':['en'],
            'postprocessors':[{'key':'FFmpegEmbedSubtitle'}],
            'progress_hooks': [my_hook],
            'ffmpeg_location':ffmpeg+'/ffmpeg'
        })
    else:
        video = False
        ydl = youtube_dl.YoutubeDL({
            'format': 'bestaudio/best',
            'outtmpl': store+'/%(title)s.%(ext)s',
            'postprocessors': [{
                'key': 'FFmpegExtractAudio',
                'preferredcodec': 'mp3',
                'preferredquality': '192',
            }],
            'ffmpeg_location':ffmpeg+'/ffmpeg',
            'progress_hooks': [my_hook]
        })
    with ydl:
        ydl.download([link])


def my_hook(d):
    global act,flag
    if d['status'] == 'finished':
        act.doneDownload(True)
        if flag:
            act.filename = (d['filename'].split('/')[-1]).split('.')[0]+('.mkv' if  video else '.mp3')
            flag = False
        return
    if d['status'] == 'error':
        act.doneDownload(False)
        return
    if d['status'] == 'downloading':
        if flag:
            if (d['total_bytes'] == None) & (d['downloaded_bytes'] == None):
                act.updateStatus(-1,"")
            else:
                string = "{}\n{} MB/{} MB \nETA:{}".format(d['filename'].split('/')[-1],d['downloaded_bytes']/1000000,
                                                           d['total_bytes']/1000000,d['eta'])
                act.updateStatus(int(100*d['downloaded_bytes']/d['total_bytes']),string)



