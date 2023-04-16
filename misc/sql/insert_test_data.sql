INSERT INTO account (id, email, password, displayname, is_admin, is_active) VALUES
(
  1,
  'demo@demo.de',
  '12000:f9490883738cc151e9edde4f93b4aadb:fb7aa3d97b3bc6a2f4dd98d330356877c7065478606f5dce5be135b464c6098f', -- password: demo
  'demo-displayname',
  true,
  true
),
(
  2,
  'demo1@demo1.de',
  '12000:294c525f824641c29cf5fdabe4cb59be:8b4b103d6d04d29008b2095512b171cdd25959f9d9743e5602c8ca20d3a6151c', -- password: demo1
  'demo1-demo1',
  false,
  true
);

INSERT INTO poi (id, latitude, longitude, displayname, account_id) VALUES
(1, 53.540115, 8.583458, 'Hochschule Bremerhaven Rondell', 1),
(2, 53.539667, 8.584193, 'Wencke Dock', 1),
(3, 53.540356, 8.582042, 'CineMotion Bremerhaven', 1),
(4, 53.540896, 8.582049, 'THP', 2),
(5, 53.539583, 8.582427, 'Zwischen Haus T und S: (Umlauttest) »ÄÖÜµ«ÆẞØ↑↓←→', 2);

INSERT INTO story (id, headline, content, account_id, poi_id) VALUES
(1, '"Whatever it takes"', '"′Cause I love the adrenaline in my veins"', 1, 1),
(2, '"Pain!"', '"You made me a, you made me a believer, believer"', 2, 1),
(3, 'Believer', 'I was hoping for an indication
I was seeking higher elevation
(Aye-aye-aye, aye-aye-aye)
I′ve been shaken, waking in the night light
I''ve been breaking, hiding from the spotlight
(Aye-aye-aye, aye-aye-aye)

The more I stray, the less I fear
And the more I reach, the more I fade away
The darkness right in front of me
Oh, it′s calling out, and I won''t walk away

I would always open up the door
Always looking up for higher floors
Want to see it all, give me more (rise, rise up)
I was always up for making changes
Walking down the street meeting strangers
Flipping through my life, turning pages (rise, rise up)


Like a prayer that only needs a reason
Like a hunter waiting for the season
(Aye-aye-aye, aye-aye-aye)
I was there, but I was always leaving
I''ve been living, but I was never breathing
(Aye-aye-aye, aye-aye-aye)

The more I stray, the less I fear
And the more I reach, the more I fade away
The darkness right in front of me
Oh, it′s calling out, and I won′t walk away

I would always open up the door
Always looking up for higher floors
Want to see it all, give me more (rise, rise up)
I was always up for making changes
Walking down the street meeting strangers
Flipping through my life, turning pages (rise, rise up)

I''m bursting like the Fourth of July
So color me and blow me away
I′m broken in the prime of my life
So embrace it and leave me to stray


I would always open up the door
Always looking up for higher floors
Want to see it all, give me more (rise, rise up)
I was always up for making changes
Walking down the street meeting strangers
Flipping through my life, turning pages (rise, rise up)

I would always open up the door
Always looking up for higher floors
Want to see it all, give me more (rise, rise up)
I was always up for making changes
Walking down the street meeting strangers
Flipping through my life, turning pages (rise, rise up)

(Rise, rise up)

(Rise, rise up)', 1, 2),
(4, 'Lorem Ipsum', 'Quam minima unde et fuga soluta rerum eos. Sed consequatur vitae maxime. Deleniti illum rerum sit maiores nobis. Consequatur dolorum molestiae incidunt consequatur. Veritatis molestiae hic minus.

Laudantium similique tempora eius et minus. Qui molestiae dolorem molestiae temporibus est aut. Voluptatem est ad ab quae. Veniam hic voluptas et.

Sint omnis maiores accusantium dignissimos ut possimus quia. Animi vel mollitia corrupti ab aliquid ipsa. Nam itaque reiciendis consequatur odio possimus est facilis. Aut minima repellendus omnis neque perferendis labore harum. Omnis velit quibusdam architecto inventore et ullam.

Est dolore adipisci quos recusandae dolores atque. Nisi ea eum veritatis. Quia consequatur fugiat aut veritatis esse. Harum amet sit doloribus. Odit nulla tempore modi. Ut voluptas ea expedita rerum voluptate repellendus voluptas reiciendis.

Hic similique amet similique soluta porro consequatur qui. Laborum est porro dignissimos nostrum. Delectus laborum optio alias iusto. Neque perferendis facere eligendi et quis accusamus laborum necessitatibus. Nisi reiciendis nostrum quis deserunt tempore qui in.', 2, 2),
(5, 'Random Characters', 'qwertuioopüasdfghölx.n.,xbckbö.jöi4298796876078&ŁÆŁ§ÐØŊ‘Ŋ™±¡£⅛⅞¤±™⅞⅝±™⅞↑‚‹©Ł‚', 2, 3),
(6, 'Crush', 'I can′t focus on what needs to get done
I''m on notice, hoping that you don′t run
You think I''m tepid but I''m misdiagnosed
′Cause I′m a stalker, I seen all of your posts, ah

And I''m just tryna play it cool now
But that′s not what I wanna do now
And I''m not tryna be with you now, you now
Mm-hmm

You make it difficult to not overthink
And when I′m with you I turn all shades of pink
I wanna touch you but don''t wanna be weird
It′s such a rush, I''m thinking ''wish you were here′, ah

And I′m just tryna play it cool now
But that''s not what I wanna do now
And I′m not tryna be with you now, you now

But I could be your crush, like
Throw you for a rush, like
Hoping you''d text me so I could tell you
I been thinking ′bout your touch, like


Touch, touch, touch, touch, touch
I could be your crush, crush, crush, crush, crush
I got a fascination with your presentation
Making me feel like you''re on my island
You′re my permanent vacation
Touch, touch, touch, touch, touch
I could be your crush, crush, crush, crush, crush
Sorry

I fill my calendar with stuff I can do
Maybe if I''m busy it could keep me from you
And I''m pretending you ain′t been on my mind
But I took an interest in the things that you like, ah

And I′m just tryna play it cool now
But that''s not what I wanna do now
And I′m not tryna be with you now, you now

But I could be your crush, like
Throw you for a rush, like
Hoping you''d text me so I could tell you
I been thinking ′bout your touch like

Touch, touch, touch, touch, touch
I could be your crush, crush, crush, crush, crush
I got a fascination with your presentation
Making me feel like you''re on my island
You′re my permanent vacation
Touch, touch, touch, touch, touch
I could be your crush, crush, crush, crush, crush


And yeah it''s true that I''m a little bit intense, right
But can you blame me when you keep me on the fence? Like
And I′ve been waiting, hoping that you′d wanna text, like
Text like ("It''s what I was born to do")
And yeah it′s true that I''m a little bit intense, right
But can you blame me when you keep me on the fence? Like
And I′ve been waiting, hoping that you''d wanna text, like ("Hey)
Text like (uh)

And I′m just tryna play it cool now
But that''s not what I wanna do now
And I''m not tryna be with you now, you now

But I could be your crush, like
Throw you for a rush, like
Hoping you′d text me so I could tell you
I been thinking ′bout your touch like

Touch, touch, touch, touch, touch
I could be your crush, crush, crush, crush, crush
I got a fascination with your presentation
Making me feel like you''re on my island
You′re my permanent vacation
Touch, touch, touch, touch, touch
I could be your crush, crush, crush, crush, crush
Sorry', 1, 3),
(7, '竜とそばかすの姫 (Ryū to Sobakasu no Hime)', '(Major Spoilers) Teenager Suzu Naito lives in the rural Kōchi Prefecture of Japan with a lost passion for singing and writing songs. When Suzu was young, she witnessed her mother rescue a child from a flooding river at the cost of her own life, causing her to resent her mother for "abandoning" her for a stranger''s child and eventually grow distant from her father. She remains in contact with a group of older choir teachers who were her mother''s friends. She is alienated from most of her classmates, with the exception of her childhood friend and self-appointed protector Shinobu Hisatake, on whom she has a crush; popular girl Ruka Watanabe; sportsman classmate Shinjiro “Kamishin” Chikami; and her genius best friend Hiroka "Hiro" Betsuyaku.
Urged by Hiro, Suzu signs into the popular virtual metaverse known as "U" and is appointed a beautiful avatar with freckles (through the AI engine''s biometric analysis[11]) whom she names "Bell", the English translation of her own name. Upon logging into U, she finds herself capable of singing again. With the assistance of Hiro, who has appointed herself Bell''s manager and producer, Bell becomes a big hit and people start to refer to her as "Belle", meaning "beautiful" in French.
During one of Belle''s concerts, an infamously strong and near-unbeatable user called "The Dragon" (or "The Beast") arrives. This prompts a vigilante group named the Justices, led by the self-righteous Justin, to begin hunting the Dragon, accusing him of disturbing the peace of U. Justin plans to unveil the Dragon''s identity to the public using a specialized program. Intrigued by the Dragon, Suzu begins to gather information about him. She discovers that he is popular amongst children, who consider him to be their hero, particularly an 8th grader named Tomo who was in the news following his mother''s death. Belle searches U for the Dragon and is led to the Dragon''s hidden castle by a mysterious angel avatar. She meets the Dragon and his five guardian AIs. Belle and the Dragon grow close. In the real world, Ruka confides to Suzu that she likes Kamishin. With Suzu''s help, the two confess their feelings.
In U, Justin captures and interrogates Belle, threatening to unveil her identity to the world if she refuses to cooperate. The Dragon''s AIs rescue Belle but their intervention allows Justin and his group to locate the Dragon''s castle and destroy it. The Dragon flees before Belle can help. Suzu and Hiro work to find out the Dragon''s real identity before Justin can and warn him. They find a live video feed of Tomo singing a song only Belle and the Dragon know, and realize that Tomo is the angel avatar, and his older brother Kei is the Dragon. Kei and Tomo are being physically and mentally abused by their father; Kei''s anger and protectiveness over Tomo is what gives The Dragon his unbeatable strength in U. Suzu contacts Kei to help but Kei does not believe that she is Belle. Shinobu, Ruka, Kamishin, and the choir teachers reveal their knowledge of Belle''s true identity and urge Suzu to sing to gain Kei''s trust. Suzu unveils herself to the world in U and begins to sing. Seeing this, Kei decides to trust her and tries to contact her again. Kei''s father sees the video of his abuse posted online and cuts off the internet connection before Kei can tell Suzu their address.
Using context clues, Ruka and Kamishin deduce that Kei''s hometown is Kawasaki, Kanagawa, near Tokyo. Since the authorities cannot intervene on abuse charges until 48 hours have passed, Suzu locates Kei and Tomo on her own and protects them from their father. The next day, she and her father warmly greet each other at the station. Shinobu praises Suzu for her bravery and decides she no longer needs his protection, feeling free to pursue the romantic relationship he has always wanted with her. Finally understanding her mother''s selfless actions, Suzu comes to terms with her mother''s death and is ready to sing with her friends.', 1, 5),
(8, '❶❷❸❺❻❼❽❾❿➢❡', 'Lorem Ipsum', 1, 5),
(9, 'ellie/atuin', 'Atuin is named after "The Great A''Tuin", a giant turtle from Terry Pratchett''s Discworld series of books.', 1, 5),
(10, 'The Last of Us Zitate', '"What did the green grape say to the purple grape? Breathe, you idiot!" – Ellie

"There are a million ways we should’ve died before today, and a million ways we can die before tomorrow." – Riley

"You know, as bad as those things are, at least they’re predictable. It’s the normal people that scare me." – Bill
', 1, 1),
(11, 'Bad Ideas', '
I hope that you don''t think I''m rude
But I wanna make out with you
And I''m a little awkward, sure
But I could touch my face to yours, oh
And no one ever called me smooth
But I just wanna see the grooves between your hands, your teeth, oh
Tell me, do you think about me?

I just wanna kiss you
And even if I miss you
At least I''ll know what it''s like to have held your hand, oh!
No-o, hey!


Bad ideas, ay (Oh!)
I know where they lead (Hey, oh!)
But I have too many to sleep
And I can''t get enough, no
I wanna kiss you standing up
Oh-oh, no!
And if tomorrow makes me low (No!)
Well it''d be worth it just to know
''Cause I can''t get enough, no
I wanna kiss you standing up
O-oh!
Hey!

I don''t know what compels me
To do the very thing that fells me
I wake up, still high on you
But by the night, I''m crashing through, so

So why''d I wanna kiss you
Even though I miss you
Guess I just wanted to know what it would feel like, oh!
No-o, hey!

Bad ideas, ay (Oh!)
I know where they lead (Hey, oh!)
But I have too many to sleep
And I can''t get enough, no
I wanna kiss you standing up
O-oh, no!
And if tomorrow makes me low (No!)
Well it''d be worth it just to know
''Cause I can''t get enough, no
I wanna kiss you standing up
O-oh!


Smitten''s a bad look on me
And if I''m talking honestly
It takes everything I got not to text, and
I just want a kiss to get me through
''Cause now all my bedsheets smell like you, so

If you think you miss me
Come on back and kiss me
I just gotta know what you and I would feel like, oh!
No-o, hey!
Yikes

Bad ideas, ay (Oh!)
I know where they lead (Hey, oh!)
But I have too many to sleep
And I can''t get enough, no
I wanna kiss you standing up
Oh-oh, no!
And if tomorrow makes me low (No!)
Well it''d be worth it just to know
''Cause I can''t get enough, no-o
I wanna kiss you standing up
Oh-oh!', 1, 1),
(12, 'headline:AÄOÖUÜªÆẞÐ›‹©‚‘', 'content:AÄOÖUÜªÆẞÐ›‹©‚‘', 2, 5);

INSERT INTO file (filename, story_id, mime_type) VALUES
('1669300756375_2f0a4873-d9ec-4458-b439-57ff4b64d952', 1, 'image/png'),
('1669908582049_cd6cd991-a4fe-4546-b9ec-4b14bdecd19a', 1, 'image/png'),
('1669910268857_62766138-8d67-4262-bf7f-7507236a0b7c', 2, 'image/png'),
('1669910285271_db793600-7513-4e10-848f-4797fe3b7d41', 3, 'image/png');
