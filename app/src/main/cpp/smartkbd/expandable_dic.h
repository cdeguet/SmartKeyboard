#ifndef smartkbd_EXPANDABLE_DIC_H
#define smartkbd_EXPANDABLE_DIC_H

namespace smartkbd {

class ExpandableDictionary {
public:
    ExpandableDictionary();
    ~ExpandableDictionary();

	void addWord(const unsigned short *word, int len, int freq);
	int getWordFrequency(const unsigned short *word, int len);
	int increaseWordFrequency(const unsigned short *word, int len);
    int getSuggestions(int *codes, int codesSize, unsigned short *outWords, int *frequencies,
        int maxWordLength, int maxWords, int maxAlternatives, int skipPos, bool modeT9,
        int *nextLetters, int nextLettersSize);

private:

    class Node {
	public:
        unsigned short code;
        int frequency;
        bool terminal;

        Node **children;
        int length;
        static const int INCREMENT = 2;

        Node();
        ~Node();
        void add(Node *n);
    };

	Node mRoots;
    int *mFrequencies;
    int mMaxWords;
    int mMaxWordLength;
    unsigned short *mOutputChars;
    int *mInputCodes;
    int mInputLength;
    int mMaxAlternatives;
    unsigned short mWord[128];
    int mSkipPos;
    int mMaxEditDistance;
    int mTypedLetterMultiplier;
    bool mT9;
    int *mNextLettersFrequencies;
    int mNextLettersSize;

	void addWordRec(Node *node, const unsigned short *word, int depth, int len, int freq);
	int getWordFrequencyRec(Node *parent, const unsigned short *word, int offset, int len);
	int increaseWordFrequencyRec(Node *parent, const unsigned short *word, int offset, int len);
    void getWordsRec(Node *parent, int depth, int maxDepth, bool completion, int snr, int inputIndex);
    void registerNextLetter(unsigned short c);
    bool addSuggestion(unsigned short *word, int length, int frequency);
};

}; // namespace smartkbd

#endif
