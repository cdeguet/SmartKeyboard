#include "expandable_dic.h"
#include "dictionary.h"
#include <string.h>

#include <android/log.h> 

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "SmartKeyboard", __VA_ARGS__) 

using namespace smartkbd;


ExpandableDictionary::Node::Node(): code(0), frequency(0), terminal(false), children(0), length(0) 
{
}

ExpandableDictionary::Node::~Node()
{
	for (int i = 0; i < length; ++i)
	{
		delete children[i];
	}
	delete[] children;
}

void ExpandableDictionary::Node::add(Node *n) 
{
	if (children == 0)
	{
		children = new Node*[INCREMENT];
	}
	else if (length + 1 > sizeof(children) / sizeof(Node*)) 
	{
		// Resize children array if needed
    	Node **tempData = new Node*[length + INCREMENT];
        if (length > 0) 
		{
			for (int i = 0; i < length; ++i)
			{
				tempData[i] = children[i];
			}
        }
		delete[] children;
        children = tempData;
    }
    children[length++] = n;
}

ExpandableDictionary::ExpandableDictionary()
{
}

ExpandableDictionary::~ExpandableDictionary()
{
}

void ExpandableDictionary::addWord(const unsigned short *word, int len, int freq)
{
	addWordRec(&mRoots, word, 0, len, freq);
}

int ExpandableDictionary::getWordFrequency(const unsigned short *word, int len)
{
	return getWordFrequencyRec(&mRoots, word, 0, len);
}

int ExpandableDictionary::increaseWordFrequency(const unsigned short *word, int len)
{
	return increaseWordFrequencyRec(&mRoots, word, 0, len);
}


int ExpandableDictionary::getSuggestions(int *codes, int codesSize, unsigned short *outWords, int *frequencies,
        int maxWordLength, int maxWords, int maxAlternatives, int skipPos, bool modeT9,
        int *nextLetters, int nextLettersSize)
{
    int suggWords = 0;
    mFrequencies = frequencies;
    mOutputChars = outWords;
    mInputCodes = codes;
    mInputLength = codesSize;
    mMaxAlternatives = maxAlternatives;
    mMaxWordLength = maxWordLength;
    mMaxWords = maxWords;
    mSkipPos = skipPos;
    mMaxEditDistance = mInputLength < 5 ? 2 : mInputLength / 2;
    mT9 = modeT9;
    mNextLettersFrequencies = nextLetters;
    mNextLettersSize = nextLettersSize;

    getWordsRec(&mRoots, 0, mInputLength * 3, false, 1, 0);

    // Get the word count
    while (suggWords < mMaxWords && mFrequencies[suggWords] > 0) suggWords++;
    return suggWords;
}


void ExpandableDictionary::addWordRec(Node *node, const unsigned short *word, int depth, int len, int freq)
{
        if (depth >= len) {
			// Should not happen!!!
        	return;
        }
        unsigned short c = word[depth];
        // Does children have the current character?
        Node *childNode = 0;
        bool found = false;
        for (int i = 0; i < node->length; i++) {
            childNode = node->children[i];
            if (childNode->code == c) {
                found = true;
                break;
            }
        }
        if (!found) {
            childNode = new Node();
            childNode->code = c;
            node->add(childNode);
        }
        if (len == depth + 1) {
            // Terminate this word
            childNode->terminal = true;
            childNode->frequency = freq > childNode->frequency ? freq : childNode->frequency;
            if (childNode->frequency > 255) childNode->frequency = 255;
            return;
        }
        addWordRec(childNode, word, depth + 1, len, freq);
}


int ExpandableDictionary::getWordFrequencyRec(Node *parent, const unsigned short *word, int offset, int length)
{
	int count = parent->length;
    unsigned short currentChar = word[offset];
	for (int j = 0; j < count; j++) {
		Node *node = parent->children[j];
        if (node->code == currentChar) {
        	if (offset == length - 1) {
            	if (node->terminal) {
                	return node->frequency;
                }
            } else {
            	if (node->children != 0) {
                	int freq = getWordFrequencyRec(node, word, offset + 1, length);
                    if (freq > -1) return freq;
                }
            }
        }
    }
    return -1;
}


int ExpandableDictionary::increaseWordFrequencyRec(Node *parent, const unsigned short *word, int offset, int length)
{
	int count = parent->length;
    unsigned short currentChar = word[offset];
	for (int j = 0; j < count; j++) {
		Node *node = parent->children[j];
        if (node->code == currentChar) {
        	if (offset == length - 1) {
            	if (node->terminal) {
                	return ++node->frequency;
                }
            } else {
            	if (node->children != 0) {
                	int freq = increaseWordFrequencyRec(node, word, offset + 1, length);
                    if (freq > -1) return freq;
                }
            }
        }
    }
    return -1;
}

void
ExpandableDictionary::registerNextLetter(unsigned short c)
{
    if (c < mNextLettersSize) {
        mNextLettersFrequencies[c]++;
    }
}

bool
ExpandableDictionary::addSuggestion(unsigned short *word, int length, int frequency)
{
    word[length] = 0;

    // Find the right insertion point
    int insertAt = 0;
    while (insertAt < mMaxWords) {
        if (frequency > mFrequencies[insertAt]
                 || (mFrequencies[insertAt] == frequency
                     && length < Dictionary::wideStrLen(mOutputChars + insertAt * mMaxWordLength))) {
            break;
        }
        insertAt++;
    }
    if (insertAt < mMaxWords) {
        memmove((char*) mFrequencies + (insertAt + 1) * sizeof(mFrequencies[0]),
               (char*) mFrequencies + insertAt * sizeof(mFrequencies[0]),
               (mMaxWords - insertAt - 1) * sizeof(mFrequencies[0]));
        mFrequencies[insertAt] = frequency;
        memmove((char*) mOutputChars + (insertAt + 1) * mMaxWordLength * sizeof(short),
               (char*) mOutputChars + (insertAt    ) * mMaxWordLength * sizeof(short),
               (mMaxWords - insertAt - 1) * sizeof(short) * mMaxWordLength);
        unsigned short *dest = mOutputChars + (insertAt    ) * mMaxWordLength;
        while (length--) {
            *dest++ = *word++;
        }
        *dest = 0; // NULL terminate
        return true;
    }
    return false;
}


void
ExpandableDictionary::getWordsRec(Node *parent, int depth, int maxDepth, bool completion, int snr, int inputIndex)
{
    if (!mT9 && depth > maxDepth) {
        return;
    }
    int count = parent->length;
    int *currentChars = 0;
    if (mInputLength <= inputIndex) {
        completion = true;
    } else {
        currentChars = mInputCodes + (inputIndex * mMaxAlternatives);
    }

    for (int i = 0; i < count; i++) {
		Node *node = parent->children[i];
        unsigned short c = node->code;
        unsigned short lowerC = Dictionary::toLowerCase(c);
        bool terminal = node->terminal;
        int freq = 1;
        if (terminal) freq = node->frequency;
        // If we are only doing completions, no need to look at the typed characters.
        if (completion) {
            mWord[depth] = c;
            if (terminal) {
                addSuggestion(mWord, depth + 1, freq * snr);
                if (depth >= mInputLength && mSkipPos < 0) {
                    registerNextLetter(mWord[mInputLength]);
                }
            }
            if (node->children != 0) {
                getWordsRec(node, depth + 1, maxDepth, completion, snr, inputIndex);
            }
        } else if (c == '\'' && currentChars[0] != '\'' || mSkipPos == depth) {
            // Skip the ' or other letter and continue deeper
            mWord[depth] = c;
            if (node->children != 0) {
                getWordsRec(node, depth + 1, maxDepth, false, snr, inputIndex);
            }
        } else {
            int j = 0;
            while (currentChars[j] > 0) {
                if (currentChars[j] == lowerC || currentChars[j] == c) {
                    int addedWeight = 1;
                    if (!mT9 && j == 0) addedWeight = 2;
                    mWord[depth] = c;
                    if (mInputLength == inputIndex + 1) {
                        if (terminal) {
                            /*if (INCLUDE_TYPED_WORD_IF_VALID ||
                                !sameAsTyped(mWord, depth + 1)) {*/
                                int finalFreq = freq * snr * addedWeight;
                                if (mSkipPos < 0) finalFreq *= 2; //mFullWordMultiplier
                                addSuggestion(mWord, depth + 1, finalFreq);
                            //}
                        }
                        if (node->children != 0) {
                            getWordsRec(node, depth + 1,
                                    maxDepth, true, snr * addedWeight, inputIndex + 1);
                        }
                    } else if (node->children != 0) {
                        getWordsRec(node, depth + 1, maxDepth,
                                false, snr * addedWeight, inputIndex + 1);
                    }
                }
                j++;
                if (mSkipPos >= 0) break;
            }
        }
    }
}

